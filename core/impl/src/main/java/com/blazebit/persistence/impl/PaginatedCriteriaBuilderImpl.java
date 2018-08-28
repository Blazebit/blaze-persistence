/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.CaseWhenStarterBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.HavingOrBuilder;
import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.builder.object.DelegatingKeysetExtractionObjectBuilder;
import com.blazebit.persistence.impl.builder.object.KeysetExtractionObjectBuilder;
import com.blazebit.persistence.impl.function.pageposition.PagePositionFunction;
import com.blazebit.persistence.impl.keyset.KeysetMode;
import com.blazebit.persistence.impl.keyset.KeysetPaginationHelper;
import com.blazebit.persistence.impl.keyset.SimpleKeysetLink;
import com.blazebit.persistence.impl.query.CTENode;
import com.blazebit.persistence.impl.query.CustomQuerySpecification;
import com.blazebit.persistence.impl.query.CustomSQLTypedQuery;
import com.blazebit.persistence.impl.query.EntityFunctionNode;
import com.blazebit.persistence.impl.query.ObjectBuilderTypedQuery;
import com.blazebit.persistence.impl.query.QuerySpecification;

import javax.persistence.Parameter;
import javax.persistence.TypedQuery;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class PaginatedCriteriaBuilderImpl<T> extends AbstractFullQueryBuilder<T, PaginatedCriteriaBuilder<T>, PaginatedCriteriaBuilderImpl<T>, PaginatedCriteriaBuilderImpl<T>, BaseFinalSetOperationBuilderImpl<T, ?, ?>> implements PaginatedCriteriaBuilder<T> {

    private static final String ENTITY_PAGE_POSITION_PARAMETER_NAME = "_entityPagePositionParameter";
    private static final String PAGE_POSITION_ID_QUERY_ALIAS_PREFIX = "_page_position_";
    private static final Set<ClauseType> ID_QUERY_CLAUSE_EXCLUSIONS = EnumSet.of(ClauseType.SELECT);
    private static final Set<ClauseType> ID_QUERY_GROUP_BY_CLAUSE_EXCLUSIONS = EnumSet.of(ClauseType.SELECT, ClauseType.GROUP_BY);
    private static final Set<ClauseType> OBJECT_QUERY_CLAUSE_EXCLUSIONS = EnumSet.complementOf(EnumSet.of(ClauseType.ORDER_BY, ClauseType.SELECT));
    private static final ResolvedExpression[] EMPTY = new ResolvedExpression[0];

    private boolean keysetExtraction;
    private boolean withCountQuery = true;
    private boolean withForceIdQuery = false;
    private int highestOffset = 0;
    private final KeysetPage keysetPage;
    private final ResolvedExpression[] identifierExpressions;

    // Mutable state
    private final Object entityId;
    private boolean needsNewIdList;
    private int[] keysetToSelectIndexMapping;
    private String[] identifierToUseSelectAliases;
    private KeysetMode keysetMode;

    // Cache
    private String cachedIdQueryString;
    private String cachedExternalIdQueryString;

    public PaginatedCriteriaBuilderImpl(AbstractFullQueryBuilder<T, ? extends FullQueryBuilder<T, ?>, ?, ?, ?> baseBuilder, boolean keysetExtraction, Object entityId, int pageSize, ResolvedExpression[] identifierExpressions) {
        super(baseBuilder);
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize may not be zero or negative");
        }
        this.keysetExtraction = keysetExtraction;
        this.keysetPage = null;
        this.entityId = entityId;
        this.maxResults = pageSize;
        this.identifierExpressions = identifierExpressions;
        updateKeysetMode();
    }

    public PaginatedCriteriaBuilderImpl(AbstractFullQueryBuilder<T, ? extends FullQueryBuilder<T, ?>, ?, ?, ?> baseBuilder, boolean keysetExtraction, KeysetPage keysetPage, int firstRow, int pageSize, ResolvedExpression[] identifierExpressions) {
        super(baseBuilder);
        if (firstRow < 0) {
            throw new IllegalArgumentException("firstRow may not be negative");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize may not be zero or negative");
        }
        this.keysetExtraction = keysetExtraction;
        this.keysetPage = keysetPage;
        this.firstResult = firstRow;
        this.entityId = null;
        this.maxResults = pageSize;
        this.identifierExpressions = identifierExpressions;
        updateKeysetMode();
    }

    @Override
    public <Y> PaginatedCriteriaBuilder<Y> copy(Class<Y> resultClass) {
        FullQueryBuilder<Y, ?> criteriaBuilder = super.copy(resultClass);
        PaginatedCriteriaBuilder<Y> builder;
        if (entityId != null) {
            builder = criteriaBuilder.page(entityId, maxResults);
        } else if (keysetPage != null) {
            builder = criteriaBuilder.page(keysetPage, firstResult, maxResults);
        } else {
            builder = criteriaBuilder.page(firstResult, maxResults);
        }

        builder.withKeysetExtraction(keysetExtraction);
        return builder;
    }

    @Override
    public PaginatedCriteriaBuilder<T> setFirstResult(int firstResult) {
        super.setFirstResult(firstResult);
        updateKeysetMode();
        return this;
    }

    @Override
    public PaginatedCriteriaBuilder<T> setMaxResults(int maxResults) {
        super.setMaxResults(maxResults);
        updateKeysetMode();
        return this;
    }

    private void updateKeysetMode() {
        KeysetMode oldMode = this.keysetMode;
        this.keysetMode = KeysetPaginationHelper.getKeysetMode(keysetPage, entityId, firstResult, maxResults);
        if (keysetMode == KeysetMode.NONE) {
            this.keysetManager.setKeysetLink(null);
        } else if (keysetMode == KeysetMode.NEXT) {
            this.keysetManager.setKeysetLink(new SimpleKeysetLink(keysetPage.getHighest(), keysetMode));
        } else {
            this.keysetManager.setKeysetLink(new SimpleKeysetLink(keysetPage.getLowest(), keysetMode));
        }

        if (keysetMode != oldMode) {
            prepareForModification(ClauseType.WHERE);
        }
    }

    @Override
    public PaginatedCriteriaBuilder<T> withKeysetExtraction(boolean keysetExtraction) {
        this.keysetExtraction = keysetExtraction;
        return this;
    }

    @Override
    public boolean isKeysetExtraction() {
        return keysetExtraction;
    }

    @Override
    public PaginatedCriteriaBuilder<T> withCountQuery(boolean withCountQuery) {
        this.withCountQuery = withCountQuery;
        return this;
    }

    @Override
    public boolean isWithCountQuery() {
        return withCountQuery;
    }

    @Override
    public PaginatedCriteriaBuilder<T> withForceIdQuery(boolean withForceIdQuery) {
        this.withForceIdQuery = withForceIdQuery;
        return this;
    }

    @Override
    public boolean isWithForceIdQuery() {
        return withForceIdQuery;
    }

    @Override
    public PaginatedCriteriaBuilder<T> withHighestKeysetOffset(int offset) {
        this.highestOffset = offset;
        return this;
    }

    @Override
    public int getHighestKeysetOffset() {
        return highestOffset;
    }

    @Override
    protected ResolvedExpression[] getIdentifierExpressions() {
        if (identifierExpressions != null) {
            return identifierExpressions;
        } else if (hasGroupBy) {
            return getGroupByIdentifierExpressions();
        } else {
            return getQueryRootEntityIdentifierExpressions();
        }
    }

    private <X> TypedQuery<X> getCountQuery(String countQueryString, Class<X> resultType, boolean normalQueryMode, Set<JoinNode> keyRestrictedLeftJoins) {
        if (normalQueryMode && isEmpty(keyRestrictedLeftJoins, COUNT_QUERY_CLAUSE_EXCLUSIONS)) {
            TypedQuery<X> countQuery = em.createQuery(countQueryString, resultType);
            if (isCacheable()) {
                mainQuery.jpaProvider.setCacheable(countQuery);
            }
            parameterManager.parameterizeQuery(countQuery);
            return countQuery;
        }

        TypedQuery<X> baseQuery = em.createQuery(countQueryString, resultType);
        Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery);
        List<String> keyRestrictedLeftJoinAliases = getKeyRestrictedLeftJoinAliases(baseQuery, keyRestrictedLeftJoins, COUNT_QUERY_CLAUSE_EXCLUSIONS);
        List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(baseQuery);
        boolean shouldRenderCteNodes = renderCteNodes(false);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(false) : Collections.EMPTY_LIST;
        QuerySpecification querySpecification = new CustomQuerySpecification(
                this, baseQuery, parameterManager.getParameters(), parameterListNames, null, null, keyRestrictedLeftJoinAliases, entityFunctionNodes, mainQuery.cteManager.isRecursive(), ctes, shouldRenderCteNodes
        );

        TypedQuery<X> countQuery = new CustomSQLTypedQuery<X>(
                querySpecification,
                baseQuery,
                parameterManager.getTransformers(),
                parameterManager.getValuesParameters(),
                parameterManager.getValuesBinders()
        );

        parameterManager.parameterizeQuery(countQuery);
        return countQuery;
    }

    @Override
    public PaginatedTypedQueryImpl<T> getQuery() {
        prepareAndCheck();
        // We can only use the query directly if we have no ctes, entity functions or hibernate bugs
        Set<JoinNode> keyRestrictedLeftJoins = joinManager.getKeyRestrictedLeftJoins();
        boolean normalQueryMode = !isMainQuery || (!mainQuery.cteManager.hasCtes() && !joinManager.hasEntityFunctions() && keyRestrictedLeftJoins.isEmpty());
        TypedQuery<?> countQuery = null;
        String countQueryString = getPageCountQueryStringWithoutCheck();

        if (entityId == null) {
            // No reference entity id, so just do a simple count query
            countQuery = getCountQuery(countQueryString, Long.class, normalQueryMode, keyRestrictedLeftJoins);
        } else {
            countQuery = getCountQuery(countQueryString, Object[].class, normalQueryMode, keyRestrictedLeftJoins);
        }

        TypedQuery<?> idQuery = null;
        TypedQuery<T> objectQuery;
        KeysetExtractionObjectBuilder<T> objectBuilder;
        if (hasCollections || withForceIdQuery) {
            String idQueryString = getPageIdQueryStringWithoutCheck();
            idQuery = getIdQuery(idQueryString, normalQueryMode, keyRestrictedLeftJoins);
            objectQuery = getObjectQueryById(normalQueryMode, keyRestrictedLeftJoins);
            objectBuilder = null;
        } else {
            Map.Entry<TypedQuery<T>, KeysetExtractionObjectBuilder<T>> entry = getObjectQuery(normalQueryMode, keyRestrictedLeftJoins);
            objectQuery = entry.getKey();
            objectBuilder = entry.getValue();
        }
        PaginatedTypedQueryImpl<T> query = new PaginatedTypedQueryImpl<>(
                withCountQuery,
                highestOffset,
                countQuery,
                idQuery,
                objectQuery,
                objectBuilder,
                parameterManager.getParameters(),
                entityId,
                firstResult,
                maxResults,
                getIdentifierExpressionsToUse().length,
                needsNewIdList,
                keysetToSelectIndexMapping,
                keysetMode,
                keysetPage
        );
        return query;
    }

    @Override
    public PagedList<T> getResultList() {
        return getQuery().getResultList();
    }

    @Override
    public String getCountQueryString() {
        return getPageCountQueryString();
    }

    @Override
    public TypedQuery<Long> getCountQuery() {
        prepareAndCheck();
        // We can only use the query directly if we have no ctes, entity functions or hibernate bugs
        Set<JoinNode> keyRestrictedLeftJoins = joinManager.getKeyRestrictedLeftJoins();
        boolean normalQueryMode = !isMainQuery || (!mainQuery.cteManager.hasCtes() && !joinManager.hasEntityFunctions() && keyRestrictedLeftJoins.isEmpty());
        String countQueryString = getPageCountQueryStringWithoutCheck();
        return getCountQuery(countQueryString, Long.class, normalQueryMode, keyRestrictedLeftJoins);
    }

    @Override
    public String getPageCountQueryString() {
        prepareAndCheck();
        return getExternalPageCountQueryString();
    }

    private String getPageCountQueryStringWithoutCheck() {
        if (cachedCountQueryString == null) {
            cachedCountQueryString = buildPageCountQueryString(false, false);
        }

        return cachedCountQueryString;
    }

    protected String getExternalPageCountQueryString() {
        if (cachedExternalCountQueryString == null) {
            cachedExternalCountQueryString = buildPageCountQueryString(true, false);
        }

        return cachedExternalCountQueryString;
    }

    @Override
    public String getPageIdQueryString() {
        prepareAndCheck();
        return getExternalPageIdQueryString();
    }

    private String getPageIdQueryStringWithoutCheck() {
        if (cachedIdQueryString == null && (hasCollections || withForceIdQuery)) {
            cachedIdQueryString = buildPageIdQueryString(false);
        }

        return cachedIdQueryString;
    }

    protected String getExternalPageIdQueryString() {
        if (cachedExternalIdQueryString == null && (hasCollections || withForceIdQuery)) {
            cachedExternalIdQueryString = buildPageIdQueryString(true);
        }

        return cachedExternalIdQueryString;
    }

    @Override
    public String getQueryString() {
        prepareAndCheck();
        return getExternalQueryString();
    }

    @Override
    protected String getBaseQueryString() {
        if (cachedQueryString == null) {
            if ((hasCollections || withForceIdQuery)) {
                cachedQueryString = buildBaseQueryString(false);
            } else {
                cachedQueryString = buildObjectQueryString(false);
            }
        }

        return cachedQueryString;
    }

    protected String getExternalQueryString() {
        if (cachedExternalQueryString == null) {
            if ((hasCollections || withForceIdQuery)) {
                cachedExternalQueryString = buildBaseQueryString(true);
            } else {
                cachedExternalQueryString = buildObjectQueryString(true);
            }
        }

        return cachedExternalQueryString;
    }

    @Override
    protected void prepareForModification(ClauseType changedClause) {
        super.prepareForModification(changedClause);
        cachedIdQueryString = null;
        cachedExternalIdQueryString = null;
    }

    @Override
    protected void prepareAndCheck() {
        if (!needsCheck) {
            return;
        }

        verifyBuilderEnded();
        if (!orderByManager.hasOrderBys()) {
            throw new IllegalStateException("Pagination requires at least one order by item!");
        }

        applyImplicitJoins(null);
        // We pass true here to always generate implicit group bys for select and order by clauses. We filter these out later if necessary
        applyExpressionTransformersAndBuildGroupByClauses(true);
        hasCollections = joinManager.hasCollections();

        if (hasGroupBy) {
            if (identifierExpressions != null) {
                ResolvedExpression[] missingExpressions;
                if ((missingExpressions = findMissingExpressions(getIdentifierExpressions(), identifierExpressions)) != null) {
                    throw new IllegalStateException("Cannot paginate by expressions [" + expressionString(identifierExpressions) + "] because the expression [" + expressionString(missingExpressions) + "] is not part of the group by clause!");
                }
            }
            if (hasCollections) {
                // We register a GROUP_BY clause dependency for joins nodes of implicit grouped by expressions
                // If none of the collection join nodes appears in the group by, this means they are all aggregated somehow and thus grouped away
                boolean groupedAway = true;
                for (JoinNode joinNode : joinManager.getCollectionJoins()) {
                    if (joinNode.getClauseDependencies().contains(ClauseType.GROUP_BY)) {
                        groupedAway = false;
                        break;
                    }
                }
                if (groupedAway) {
                    hasCollections = false;
                }
            }
        }

        // Paginated criteria builders always need the last order by expression to be unique
        List<OrderByExpression> orderByExpressions = orderByManager.getOrderByExpressions(false, whereManager.rootPredicate.getPredicate(), hasGroupBy ? Arrays.asList(getIdentifierExpressions()) : Collections.<ResolvedExpression>emptyList());
        if (!orderByExpressions.get(orderByExpressions.size() - 1).isResultUnique()) {
            throw new IllegalStateException("The order by items of the query builder are not guaranteed to produce unique tuples! Consider also ordering by the entity identifier!");
        }

        if (keysetManager.hasKeyset()) {
            keysetManager.initialize(orderByExpressions);
        }

        // initialize index mappings that we use to avoid putting keyset expressions into select clauses multiple times
        if (hasCollections || withForceIdQuery) {
            ResolvedExpression[] identifierExpressionsToUse = getIdentifierExpressionsToUse();
            Map<String, Integer> identifierExpressionStringMap = new HashMap<>(identifierExpressionsToUse.length);

            for (int i = 0; i < identifierExpressionsToUse.length; i++) {
                identifierExpressionStringMap.put(identifierExpressionsToUse[i].getExpressionString(), i);
            }

            keysetToSelectIndexMapping = new int[orderByExpressions.size()];
            identifierToUseSelectAliases = new String[identifierExpressionsToUse.length];

            Integer index;
            for (int i = 0; i < orderByExpressions.size(); i++) {
                String potentialSelectAlias = orderByExpressions.get(i).getExpression().toString();
                AliasInfo aliasInfo = aliasManager.getAliasInfo(potentialSelectAlias);
                if (aliasInfo instanceof SelectInfo) {
                    index = identifierExpressionStringMap.get(((SelectInfo) aliasInfo).getExpression().toString());
                    if (index == null) {
                        keysetToSelectIndexMapping[i] = -1;
                    } else {
                        identifierToUseSelectAliases[i] = potentialSelectAlias;
                        keysetToSelectIndexMapping[i] = index;
                    }
                } else if (keysetExtraction) {
                    index = identifierExpressionStringMap.get(potentialSelectAlias);
                    keysetToSelectIndexMapping[i] = index == null ? -1 : index;
                }
            }
            if (!keysetExtraction) {
                keysetToSelectIndexMapping = null;
            }
        } else if (keysetExtraction) {
            List<SelectInfo> selectInfos = selectManager.getSelectInfos();
            Map<String, Integer> selectExpressionStringMap = new HashMap<>(selectInfos.size() * 2);
            for (int i = 0; i < selectInfos.size(); i++) {
                SelectInfo selectInfo = selectInfos.get(i);
                selectExpressionStringMap.put(selectInfo.getExpression().toString(), i);
                if (selectInfo.getAlias() != null) {
                    selectExpressionStringMap.put(selectInfo.getAlias(), i);
                }
            }

            keysetToSelectIndexMapping = new int[orderByExpressions.size()];
            identifierToUseSelectAliases = null;

            Integer index;
            for (int i = 0; i < orderByExpressions.size(); i++) {
                index = selectExpressionStringMap.get(orderByExpressions.get(i).getExpression().toString());
                keysetToSelectIndexMapping[i] = index == null ? -1 : index;
            }
        } else {
            keysetToSelectIndexMapping = null;
            identifierToUseSelectAliases = null;
        }

        // When we do keyset extraction of have complex order bys, we have to append additional expression to the end of the select clause which have to be removed later
        needsNewIdList = keysetExtraction
                || orderByManager.hasComplexOrderBys();

        // No need to do the check again if no mutation occurs
        needsCheck = false;
    }

    private ResolvedExpression[] findMissingExpressions(ResolvedExpression[] targetIdentifierExpressions, ResolvedExpression[] identifierExpressions) {
        if (targetIdentifierExpressions == null || targetIdentifierExpressions.length < identifierExpressions.length) {
            return identifierExpressions;
        }

        int identifiersSize = identifierExpressions.length;
        int targetIdentifiersSize = targetIdentifierExpressions.length;
        List<ResolvedExpression> missingExpressions = null;
        OUTER: for (int i = 0; i < identifiersSize; i++) {
            ResolvedExpression identifierExpression = identifierExpressions[i];
            for (int j = 0; j < targetIdentifiersSize; j++) {
                if (identifierExpression.equals(targetIdentifierExpressions[j])) {
                    continue OUTER;
                }
            }
            if (missingExpressions == null) {
                missingExpressions = new ArrayList<>();
            }
            missingExpressions.add(identifierExpression);
        }
        if (missingExpressions == null) {
            return null;
        }
        return missingExpressions.toArray(new ResolvedExpression[missingExpressions.size()]);
    }

    @SuppressWarnings("unchecked")
    private Map.Entry<TypedQuery<T>, KeysetExtractionObjectBuilder<T>> getObjectQuery(boolean normalQueryMode, Set<JoinNode> keyRestrictedLeftJoins) {
        String queryString = getBaseQueryString();
        Class<?> expectedResultType;

        // When the keyset is included the query obviously produces an array
        if (keysetExtraction) {
            expectedResultType = Object[].class;
        } else {
            expectedResultType = selectManager.getExpectedQueryResultType();
        }

        TypedQuery<T> query;

        if (normalQueryMode && isEmpty(keyRestrictedLeftJoins, NO_CLAUSE_EXCLUSION)) {
            query = (TypedQuery<T>) em.createQuery(queryString, expectedResultType);
            if (isCacheable()) {
                mainQuery.jpaProvider.setCacheable(query);
            }
            parameterManager.parameterizeQuery(query);
        } else {
            TypedQuery<T> baseQuery = (TypedQuery<T>) em.createQuery(queryString, expectedResultType);
            Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery);

            List<String> keyRestrictedLeftJoinAliases = getKeyRestrictedLeftJoinAliases(baseQuery, keyRestrictedLeftJoins, NO_CLAUSE_EXCLUSION);
            List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(baseQuery);
            boolean shouldRenderCteNodes = renderCteNodes(false);
            List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(false) : Collections.EMPTY_LIST;
            QuerySpecification querySpecification = new CustomQuerySpecification(
                    this, baseQuery, parameterManager.getParameters(), parameterListNames, null, null, keyRestrictedLeftJoinAliases, entityFunctionNodes, mainQuery.cteManager.isRecursive(), ctes, shouldRenderCteNodes
            );

            query = new CustomSQLTypedQuery<T>(
                    querySpecification,
                    baseQuery,
                    parameterManager.getTransformers(),
                    parameterManager.getValuesParameters(),
                    parameterManager.getValuesBinders()
            );
            parameterManager.parameterizeQuery(query);
        }

        KeysetExtractionObjectBuilder<T> objectBuilder = null;
        ObjectBuilder<T> transformerObjectBuilder = selectManager.getSelectObjectBuilder();

        if (keysetExtraction) {
            if (transformerObjectBuilder == null) {
                objectBuilder = new KeysetExtractionObjectBuilder<T>(keysetToSelectIndexMapping, keysetMode, selectManager.getExpectedQueryResultType() != Object[].class);
            } else {
                objectBuilder = new DelegatingKeysetExtractionObjectBuilder<T>(transformerObjectBuilder, keysetToSelectIndexMapping, keysetMode);
            }

            transformerObjectBuilder = objectBuilder;
        }

        if (transformerObjectBuilder != null) {
            query = new ObjectBuilderTypedQuery<>(query, transformerObjectBuilder);
        }

        return new AbstractMap.SimpleEntry<TypedQuery<T>, KeysetExtractionObjectBuilder<T>>(query, objectBuilder);
    }

    private TypedQuery<Object[]> getIdQuery(String idQueryString, boolean normalQueryMode, Set<JoinNode> keyRestrictedLeftJoins) {
        if (normalQueryMode && isEmpty(keyRestrictedLeftJoins, ID_QUERY_CLAUSE_EXCLUSIONS)) {
            TypedQuery<Object[]> idQuery = em.createQuery(idQueryString, Object[].class);
            if (isCacheable()) {
                mainQuery.jpaProvider.setCacheable(idQuery);
            }
            parameterManager.parameterizeQuery(idQuery);
            return idQuery;
        }

        TypedQuery<Object[]> baseQuery = em.createQuery(idQueryString, Object[].class);
        Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery);

        List<String> keyRestrictedLeftJoinAliases = getKeyRestrictedLeftJoinAliases(baseQuery, keyRestrictedLeftJoins, ID_QUERY_CLAUSE_EXCLUSIONS);
        List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(baseQuery);
        boolean shouldRenderCteNodes = renderCteNodes(false);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(false) : Collections.EMPTY_LIST;
        QuerySpecification querySpecification = new CustomQuerySpecification(
                this, baseQuery, parameterManager.getParameters(), parameterListNames, null, null, keyRestrictedLeftJoinAliases, entityFunctionNodes, mainQuery.cteManager.isRecursive(), ctes, shouldRenderCteNodes
        );

        TypedQuery<Object[]> idQuery = new CustomSQLTypedQuery<Object[]>(
                querySpecification,
                baseQuery,
                parameterManager.getTransformers(),
                parameterManager.getValuesParameters(),
                parameterManager.getValuesBinders()
        );
        parameterManager.parameterizeQuery(idQuery);
        return idQuery;
    }

    @SuppressWarnings("unchecked")
    private TypedQuery<T> getObjectQueryById(boolean normalQueryMode, Set<JoinNode> keyRestrictedLeftJoins) {
        ResolvedExpression[] identifierExpressionsToUse = getIdentifierExpressionsToUse();
        String skippedParameterPrefix = identifierExpressionsToUse.length == 1 ? ID_PARAM_NAME : ID_PARAM_NAME + "_";
        if (normalQueryMode && isEmpty(keyRestrictedLeftJoins, OBJECT_QUERY_CLAUSE_EXCLUSIONS)) {
            TypedQuery<T> query = (TypedQuery<T>) em.createQuery(getBaseQueryString(), selectManager.getExpectedQueryResultType());
            if (isCacheable()) {
                mainQuery.jpaProvider.setCacheable(query);
            }
            parameterManager.parameterizeQuery(query, skippedParameterPrefix);
            return applyObjectBuilder(query);
        }

        TypedQuery<T> baseQuery = (TypedQuery<T>) em.createQuery(getBaseQueryString(), selectManager.getExpectedQueryResultType());
        Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery, ID_PARAM_NAME);

        if (identifierExpressionsToUse.length == 1) {
            parameterListNames.add(ID_PARAM_NAME);
        }

        List<String> keyRestrictedLeftJoinAliases = getKeyRestrictedLeftJoinAliases(baseQuery, keyRestrictedLeftJoins, OBJECT_QUERY_CLAUSE_EXCLUSIONS);
        List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(baseQuery);
        boolean shouldRenderCteNodes = renderCteNodes(false);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(false) : Collections.EMPTY_LIST;
        Set<Parameter<?>> parameters = new HashSet<>(parameterManager.getParameters());
        if (identifierExpressionsToUse.length == 1) {
            parameters.add(baseQuery.getParameter(ID_PARAM_NAME));
        }
        QuerySpecification querySpecification = new CustomQuerySpecification(
                this, baseQuery, parameters, parameterListNames, null, null, keyRestrictedLeftJoinAliases, entityFunctionNodes, mainQuery.cteManager.isRecursive(), ctes, shouldRenderCteNodes
        );

        TypedQuery<T> query = new CustomSQLTypedQuery<T>(
                querySpecification,
                baseQuery,
                parameterManager.getTransformers(),
                parameterManager.getValuesParameters(),
                parameterManager.getValuesBinders()
        );

        parameterManager.parameterizeQuery(query, skippedParameterPrefix);

        return applyObjectBuilder(query);
    }

    @Override
    protected void appendPageCountQueryStringExtensions(StringBuilder sbSelectFrom) {
        if (entityId != null) {
            parameterManager.addParameterMapping(ENTITY_PAGE_POSITION_PARAMETER_NAME, entityId, ClauseType.SELECT, this);

            sbSelectFrom.append(", ");
            sbSelectFrom.append(mainQuery.jpaProvider.getCustomFunctionInvocation(PagePositionFunction.FUNCTION_NAME, 2));

            sbSelectFrom.append('(');
            appendSimplePageIdQueryString(sbSelectFrom);
            sbSelectFrom.append("),");

            sbSelectFrom.append(':').append(ENTITY_PAGE_POSITION_PARAMETER_NAME);
            sbSelectFrom.append(")");
        }
    }

    private String appendSimplePageIdQueryString(StringBuilder sbSelectFrom) {
        queryGenerator.setAliasPrefix(PAGE_POSITION_ID_QUERY_ALIAS_PREFIX);

        sbSelectFrom.append("SELECT ");
        appendIdentifierExpressions(sbSelectFrom);

        // TODO: actually we should add the select clauses needed for order bys
        // TODO: if we do so, the page position function has to omit select items other than the first

        List<String> whereClauseConjuncts = new ArrayList<>();
        // The id query does not have any fetch owners
        // Note that we always exclude the nodes with group by dependency. We consider just the ones from the identifiers
        Set<JoinNode> idNodesToFetch = Collections.emptySet();
        Set<JoinNode> identifierExpressionsToUseNonRootJoinNodes = getIdentifierExpressionsToUseNonRootJoinNodes();
        joinManager.buildClause(sbSelectFrom, ID_QUERY_CLAUSE_EXCLUSIONS, PAGE_POSITION_ID_QUERY_ALIAS_PREFIX, false, false, true, whereClauseConjuncts, null, explicitVersionEntities, idNodesToFetch, identifierExpressionsToUseNonRootJoinNodes);
        whereManager.buildClause(sbSelectFrom, whereClauseConjuncts, null);

        boolean inverseOrder = false;

        groupByManager.buildGroupBy(sbSelectFrom, ID_QUERY_GROUP_BY_CLAUSE_EXCLUSIONS, getIdentifierExpressionsToUse());
        havingManager.buildClause(sbSelectFrom);

        // Resolve select aliases because we might omit the select items
        orderByManager.buildOrderBy(sbSelectFrom, inverseOrder, true, false);

        queryGenerator.setAliasPrefix(null);
        return sbSelectFrom.toString();
    }

    private String buildPageIdQueryString(boolean externalRepresentation) {
        StringBuilder sbSelectFrom = new StringBuilder();
        if (externalRepresentation && isMainQuery) {
            mainQuery.cteManager.buildClause(sbSelectFrom);
        }
        buildPageIdQueryString(sbSelectFrom, externalRepresentation);
        return sbSelectFrom.toString();
    }

    private String buildPageIdQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        sbSelectFrom.append("SELECT ");
        queryGenerator.setQueryBuffer(sbSelectFrom);
        queryGenerator.setClauseType(ClauseType.SELECT);
        ResolvedExpression[] identifierExpressionsToUse = getIdentifierExpressionsToUse();

        for (int i = 0; i < identifierExpressionsToUse.length; i++) {
            identifierExpressionsToUse[i].getExpression().accept(queryGenerator);
            if (identifierToUseSelectAliases[i] != null) {
                sbSelectFrom.append(" AS ");
                sbSelectFrom.append(identifierToUseSelectAliases[i]);
            }
            sbSelectFrom.append(", ");
        }
        sbSelectFrom.setLength(sbSelectFrom.length() - 2);

        if (needsNewIdList) {
            orderByManager.buildSelectClauses(sbSelectFrom, keysetExtraction, keysetToSelectIndexMapping);
        }

        List<String> whereClauseConjuncts = new ArrayList<>();
        // The id query does not have any fetch owners
        // Note that we always exclude the nodes with group by dependency. We consider just the ones from the identifiers
        Set<JoinNode> idNodesToFetch = Collections.emptySet();
        Set<JoinNode> identifierExpressionsToUseNonRootJoinNodes = getIdentifierExpressionsToUseNonRootJoinNodes();
        joinManager.buildClause(sbSelectFrom, ID_QUERY_GROUP_BY_CLAUSE_EXCLUSIONS, null, false, externalRepresentation, true, whereClauseConjuncts, null, explicitVersionEntities, idNodesToFetch, identifierExpressionsToUseNonRootJoinNodes);

        if (keysetMode == KeysetMode.NONE) {
            whereManager.buildClause(sbSelectFrom, whereClauseConjuncts, null);
        } else {
            sbSelectFrom.append(" WHERE ");

            int positionalOffset = parameterManager.getPositionalOffset();
            if (mainQuery.getQueryConfiguration().isOptimizedKeysetPredicateRenderingEnabled()) {
                keysetManager.buildOptimizedKeysetPredicate(sbSelectFrom, positionalOffset);
            } else {
                keysetManager.buildKeysetPredicate(sbSelectFrom, positionalOffset);
            }

            if (whereManager.hasPredicates() || !whereClauseConjuncts.isEmpty()) {
                sbSelectFrom.append(" AND ");
                whereManager.buildClausePredicate(sbSelectFrom, whereClauseConjuncts, null);
            }
        }

        boolean inverseOrder = keysetMode == KeysetMode.PREVIOUS;

        // TODO: Think about optimizing this here
        // We could avoid rendering the group by clause if the collection joins aren't referenced
        // We could also build a minimal group by clause when hasGroupBy == false, otherwise we require the clause
        // The same optimization can be done in the simplePageIdQueryString
        groupByManager.buildGroupBy(sbSelectFrom, ID_QUERY_GROUP_BY_CLAUSE_EXCLUSIONS, getIdentifierExpressionsToUse());
        havingManager.buildClause(sbSelectFrom);

        // Resolve select aliases to their actual expressions only if the select items aren't included
        orderByManager.buildOrderBy(sbSelectFrom, inverseOrder, !needsNewIdList, needsNewIdList);

        // execute illegal collection access check
        orderByManager.acceptVisitor(new IllegalSubqueryDetector(aliasManager));

        return sbSelectFrom.toString();
    }

    @Override
    protected String buildBaseQueryString(boolean externalRepresentation) {
        StringBuilder sbSelectFrom = new StringBuilder();
        if (externalRepresentation && isMainQuery) {
            mainQuery.cteManager.buildClause(sbSelectFrom);
        }
        buildBaseQueryString(sbSelectFrom, externalRepresentation);
        return sbSelectFrom.toString();
    }

    @Override
    protected void buildBaseQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        selectManager.buildSelect(sbSelectFrom, false);

        /**
         * we have already selected the IDs so now we only need so select the
         * fields and apply the ordering all other clauses are not required any
         * more and therefore we can also omit any joins which the SELECT or the
         * ORDER_BY clause do not depend on
         */
        List<String> whereClauseConjuncts = new ArrayList<>();
        joinManager.buildClause(sbSelectFrom, OBJECT_QUERY_CLAUSE_EXCLUSIONS, null, false, externalRepresentation, false, whereClauseConjuncts, null, explicitVersionEntities, nodesToFetch, Collections.EMPTY_SET);
        sbSelectFrom.append(" WHERE ");

        ResolvedExpression[] identifierExpressions = getIdentifierExpressions();
        ResolvedExpression[] resultUniqueExpressions = getUniqueIdentifierExpressions();

        if (resultUniqueExpressions != null) {
            identifierExpressions = resultUniqueExpressions;
        }

        queryGenerator.setQueryBuffer(sbSelectFrom);
        if (identifierExpressions.length == 1) {
            identifierExpressions[0].getExpression().accept(queryGenerator);
            sbSelectFrom.append(" IN :").append(ID_PARAM_NAME);
        } else {
            sbSelectFrom.append('(');
            for (int i = 0; i < maxResults; i++) {
                for (int j = 0; j < identifierExpressions.length; j++) {
                    identifierExpressions[j].getExpression().accept(queryGenerator);
                    sbSelectFrom.append(" = :").append(ID_PARAM_NAME);
                    sbSelectFrom.append('_').append(j).append('_').append(i);
                    sbSelectFrom.append(" AND ");
                }
                sbSelectFrom.setLength(sbSelectFrom.length() - " AND ".length());
                sbSelectFrom.append(" OR ");
            }
            sbSelectFrom.setLength(sbSelectFrom.length() - " OR ".length());
            sbSelectFrom.append(')');
        }

        for (String conjunct : whereClauseConjuncts) {
            sbSelectFrom.append(" AND ");
            sbSelectFrom.append(conjunct);
        }

        if (hasGroupBy) {
            groupByManager.buildGroupBy(sbSelectFrom, OBJECT_QUERY_CLAUSE_EXCLUSIONS);
            havingManager.buildClause(sbSelectFrom);
        }

        orderByManager.buildOrderBy(sbSelectFrom, false, false, false);
    }

    private String buildObjectQueryString(boolean externalRepresentation) {
        StringBuilder sbSelectFrom = new StringBuilder();
        if (externalRepresentation && isMainQuery) {
            mainQuery.cteManager.buildClause(sbSelectFrom);
        }
        buildObjectQueryString(sbSelectFrom, externalRepresentation);
        return sbSelectFrom.toString();
    }

    private String buildObjectQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        selectManager.buildSelect(sbSelectFrom, false);

        if (keysetExtraction) {
            orderByManager.buildSelectClauses(sbSelectFrom, true, keysetToSelectIndexMapping);
        }

        List<String> whereClauseConjuncts = new ArrayList<>();
        joinManager.buildClause(sbSelectFrom, NO_CLAUSE_EXCLUSION, null, false, externalRepresentation, false, whereClauseConjuncts, null, explicitVersionEntities, nodesToFetch, Collections.EMPTY_SET);

        if (keysetMode == KeysetMode.NONE) {
            whereManager.buildClause(sbSelectFrom, whereClauseConjuncts, null);
        } else {
            sbSelectFrom.append(" WHERE ");

            int positionalOffset = parameterManager.getPositionalOffset();
            if (mainQuery.getQueryConfiguration().isOptimizedKeysetPredicateRenderingEnabled()) {
                keysetManager.buildOptimizedKeysetPredicate(sbSelectFrom, positionalOffset);
            } else {
                keysetManager.buildKeysetPredicate(sbSelectFrom, positionalOffset);
            }

            if (whereManager.hasPredicates() || !whereClauseConjuncts.isEmpty()) {
                sbSelectFrom.append(" AND ");
                whereManager.buildClausePredicate(sbSelectFrom, whereClauseConjuncts, null);
            }
        }

        boolean inverseOrder = keysetMode == KeysetMode.PREVIOUS;

        appendGroupByClause(sbSelectFrom);
        orderByManager.buildOrderBy(sbSelectFrom, inverseOrder, false, false);

        // execute illegal collection access check
        orderByManager.acceptVisitor(new IllegalSubqueryDetector(aliasManager));

        return sbSelectFrom.toString();
    }

    @Override
    public PaginatedCriteriaBuilder<T> distinct() {
        throw new IllegalStateException("Calling distinct() on a PaginatedCriteriaBuilder is not allowed.");
    }

    @Override
    public RestrictionBuilder<PaginatedCriteriaBuilder<T>> having(String expression) {
        throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
    }

    @Override
    public CaseWhenStarterBuilder<RestrictionBuilder<PaginatedCriteriaBuilder<T>>> havingCase() {
        throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
    }

    @Override
    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<PaginatedCriteriaBuilder<T>>> havingSimpleCase(String expression) {
        throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
    }

    @Override
    public HavingOrBuilder<PaginatedCriteriaBuilder<T>> havingOr() {
        throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
    }

    @Override
    public SubqueryInitiator<PaginatedCriteriaBuilder<T>> havingExists() {
        throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
    }

    @Override
    public SubqueryInitiator<PaginatedCriteriaBuilder<T>> havingNotExists() {
        throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
    }

    @Override
    public SubqueryBuilder<PaginatedCriteriaBuilder<T>> havingExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
    }

    @Override
    public SubqueryBuilder<PaginatedCriteriaBuilder<T>> havingNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<PaginatedCriteriaBuilder<T>>> havingSubquery() {
        throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<PaginatedCriteriaBuilder<T>>> havingSubquery(String subqueryAlias, String expression) {
        throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<PaginatedCriteriaBuilder<T>>> havingSubqueries(String expression) {
        throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<PaginatedCriteriaBuilder<T>>> havingSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<PaginatedCriteriaBuilder<T>>> havingSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
    }

    @Override
    public PaginatedCriteriaBuilder<T> setHavingExpression(String expression) {
        throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
    }

    @Override
    public MultipleSubqueryInitiator<PaginatedCriteriaBuilder<T>> setHavingExpressionSubqueries(String expression) {
        throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y> SelectObjectBuilder<PaginatedCriteriaBuilder<Y>> selectNew(Class<Y> clazz) {
        return (SelectObjectBuilder<PaginatedCriteriaBuilder<Y>>) super.selectNew(clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y> PaginatedCriteriaBuilder<Y> selectNew(ObjectBuilder<Y> builder) {
        return (PaginatedCriteriaBuilder<Y>) super.selectNew(builder);
    }
}
