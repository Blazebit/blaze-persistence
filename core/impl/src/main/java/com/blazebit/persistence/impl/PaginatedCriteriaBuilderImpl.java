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

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import com.blazebit.persistence.impl.builder.object.DelegatingKeysetExtractionObjectBuilder;
import com.blazebit.persistence.impl.builder.object.KeysetExtractionObjectBuilder;
import com.blazebit.persistence.impl.function.count.AbstractCountFunction;
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
import com.blazebit.persistence.impl.transform.ExpressionTransformerGroup;
import com.blazebit.persistence.impl.util.JpaMetamodelUtils;

import javax.persistence.TypedQuery;
import javax.persistence.metamodel.Attribute;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class PaginatedCriteriaBuilderImpl<T> extends AbstractFullQueryBuilder<T, PaginatedCriteriaBuilder<T>, PaginatedCriteriaBuilderImpl<T>, PaginatedCriteriaBuilderImpl<T>, BaseFinalSetOperationBuilderImpl<T, ?, ?>> implements PaginatedCriteriaBuilder<T> {

    private static final String ENTITY_PAGE_POSITION_PARAMETER_NAME = "_entityPagePositionParameter";
    private static final String PAGE_POSITION_ID_QUERY_ALIAS_PREFIX = "_page_position_";

    private boolean keysetExtraction;
    private final KeysetPage keysetPage;

    // Mutable state
    private final Object entityId;
    private boolean needsNewIdList;
    private KeysetMode keysetMode;

    // Cache
    private String cachedCountQueryString;
    private String cachedExternalCountQueryString;
    private String cachedIdQueryString;
    private String cachedExternalIdQueryString;

    public PaginatedCriteriaBuilderImpl(AbstractFullQueryBuilder<T, ? extends FullQueryBuilder<T, ?>, ?, ?, ?> baseBuilder, boolean keysetExtraction, Object entityId, int pageSize) {
        super(baseBuilder);
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize may not be zero or negative");
        }
        this.keysetExtraction = keysetExtraction;
        this.keysetPage = null;
        this.entityId = entityId;
        this.maxResults = pageSize;
        updateKeysetMode();
    }

    public PaginatedCriteriaBuilderImpl(AbstractFullQueryBuilder<T, ? extends FullQueryBuilder<T, ?>, ?, ?, ?> baseBuilder, boolean keysetExtraction, KeysetPage keysetPage, int firstRow, int pageSize) {
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
            prepareForModification();
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

    private <X> TypedQuery<X> getCountQuery(String countQueryString, Class<X> resultType, boolean normalQueryMode, Set<JoinNode> keyRestrictedLeftJoins) {
        if (normalQueryMode && isEmpty(keyRestrictedLeftJoins, EnumSet.of(ClauseType.ORDER_BY, ClauseType.SELECT))) {
            TypedQuery<X> countQuery = em.createQuery(countQueryString, resultType);
            if (isCacheable()) {
                jpaProvider.setCacheable(countQuery);
            }
            parameterManager.parameterizeQuery(countQuery);
            return countQuery;
        }

        TypedQuery<X> baseQuery = em.createQuery(countQueryString, resultType);
        Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery);
        List<String> keyRestrictedLeftJoinAliases = getKeyRestrictedLeftJoinAliases(baseQuery, keyRestrictedLeftJoins, EnumSet.of(ClauseType.ORDER_BY, ClauseType.SELECT));
        List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(baseQuery);
        boolean shouldRenderCteNodes = renderCteNodes(false);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(baseQuery, false) : Collections.EMPTY_LIST;
        QuerySpecification querySpecification = new CustomQuerySpecification(
                this, baseQuery, parameterListNames, null, null, keyRestrictedLeftJoinAliases, entityFunctionNodes, mainQuery.cteManager.isRecursive(), ctes, shouldRenderCteNodes
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
    public PaginatedTypedQuery<T> getQuery() {
        prepareAndCheck();
        // We can only use the query directly if we have no ctes, entity functions or hibernate bugs
        Set<JoinNode> keyRestrictedLeftJoins = joinManager.getKeyRestrictedLeftJoins();
        boolean normalQueryMode = !isMainQuery || (!mainQuery.cteManager.hasCtes() && !joinManager.hasEntityFunctions() && keyRestrictedLeftJoins.isEmpty());
        String countQueryString = getPageCountQueryStringWithoutCheck();

        TypedQuery<?> countQuery;
        if (entityId == null) {
            // No reference entity id, so just do a simple count query
            countQuery = getCountQuery(countQueryString, Long.class, normalQueryMode, keyRestrictedLeftJoins);
        } else {
            countQuery = getCountQuery(countQueryString, Object[].class, normalQueryMode, keyRestrictedLeftJoins);
        }

        TypedQuery<?> idQuery = null;
        TypedQuery<T> objectQuery;
        KeysetExtractionObjectBuilder<T> objectBuilder;
        if (joinManager.hasCollections()) {
            String idQueryString = getPageIdQueryStringWithoutCheck();
            idQuery = getIdQuery(idQueryString, normalQueryMode, keyRestrictedLeftJoins);
            objectQuery = getObjectQueryById(normalQueryMode, keyRestrictedLeftJoins);
            objectBuilder = null;
        } else {
            Map.Entry<TypedQuery<T>, KeysetExtractionObjectBuilder<T>> entry = getObjectQuery(normalQueryMode, keyRestrictedLeftJoins);
            objectQuery = entry.getKey();
            objectBuilder = entry.getValue();
        }
        PaginatedTypedQuery<T> query = new PaginatedTypedQuery<T>(countQuery, idQuery, objectQuery, objectBuilder, entityId, firstResult, maxResults, needsNewIdList, keysetExtraction, keysetMode, keysetPage);
        return query;
    }

    @Override
    public PagedList<T> getResultList() {
        return getQuery().getResultList();
    }

    @Override
    public String getPageCountQueryString() {
        prepareAndCheck();
        return getExternalPageCountQueryString();
    }

    private String getPageCountQueryStringWithoutCheck() {
        if (cachedCountQueryString == null) {
            cachedCountQueryString = buildPageCountQueryString(false);
        }

        return cachedCountQueryString;
    }

    protected String getExternalPageCountQueryString() {
        if (cachedExternalCountQueryString == null) {
            cachedExternalCountQueryString = buildPageCountQueryString(true);
        }

        return cachedExternalCountQueryString;
    }

    @Override
    public String getPageIdQueryString() {
        prepareAndCheck();
        return getExternalPageIdQueryString();
    }

    private String getPageIdQueryStringWithoutCheck() {
        if (cachedIdQueryString == null) {
            cachedIdQueryString = buildPageIdQueryString(false);
        }

        return cachedIdQueryString;
    }

    protected String getExternalPageIdQueryString() {
        if (cachedExternalIdQueryString == null) {
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
            if (!joinManager.hasCollections()) {
                cachedQueryString = buildObjectQueryString(false);
            } else {
                cachedQueryString = buildBaseQueryString(false);
            }
        }

        return cachedQueryString;
    }

    protected String getExternalQueryString() {
        if (cachedExternalQueryString == null) {
            if (!joinManager.hasCollections()) {
                cachedExternalQueryString = buildObjectQueryString(true);
            } else {
                cachedExternalQueryString = buildBaseQueryString(true);
            }
        }

        return cachedExternalQueryString;
    }

    @Override
    protected void prepareForModification() {
        super.prepareForModification();
        cachedCountQueryString = null;
        cachedExternalCountQueryString = null;
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
        applyExpressionTransformers();

        // Paginated criteria builders always need the last order by expression to be unique
        List<OrderByExpression> orderByExpressions = orderByManager.getOrderByExpressions(mainQuery.metamodel);
        if (!orderByExpressions.get(orderByExpressions.size() - 1).isUnique()) {
            throw new IllegalStateException("The last order by item must be unique!");
        }

        if (keysetManager.hasKeyset()) {
            keysetManager.initialize(orderByExpressions);
        }

        needsNewIdList = keysetExtraction || orderByManager.hasComplexOrderBys();

        // No need to do the check again if no mutation occurs
        needsCheck = false;
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

        if (normalQueryMode && isEmpty(keyRestrictedLeftJoins, EnumSet.noneOf(ClauseType.class))) {
            query = (TypedQuery<T>) em.createQuery(queryString, expectedResultType);
            if (isCacheable()) {
                jpaProvider.setCacheable(query);
            }
            parameterManager.parameterizeQuery(query);
        } else {
            TypedQuery<T> baseQuery = (TypedQuery<T>) em.createQuery(queryString, expectedResultType);
            Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery);

            List<String> keyRestrictedLeftJoinAliases = getKeyRestrictedLeftJoinAliases(baseQuery, keyRestrictedLeftJoins, EnumSet.noneOf(ClauseType.class));
            List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(baseQuery);
            boolean shouldRenderCteNodes = renderCteNodes(false);
            List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(baseQuery, false) : Collections.EMPTY_LIST;
            QuerySpecification querySpecification = new CustomQuerySpecification(
                    this, baseQuery, parameterListNames, null, null, keyRestrictedLeftJoinAliases, entityFunctionNodes, mainQuery.cteManager.isRecursive(), ctes, shouldRenderCteNodes
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
            int keysetSize = orderByManager.getOrderByCount();

            if (transformerObjectBuilder == null) {
                objectBuilder = new KeysetExtractionObjectBuilder<T>(keysetSize, keysetMode);
            } else {
                objectBuilder = new DelegatingKeysetExtractionObjectBuilder<T>(transformerObjectBuilder, keysetSize, keysetMode);
            }

            transformerObjectBuilder = objectBuilder;
        }

        if (transformerObjectBuilder != null) {
            query = new ObjectBuilderTypedQuery<>(query, transformerObjectBuilder);
        }

        return new AbstractMap.SimpleEntry<TypedQuery<T>, KeysetExtractionObjectBuilder<T>>(query, objectBuilder);
    }

    private TypedQuery<Object[]> getIdQuery(String idQueryString, boolean normalQueryMode, Set<JoinNode> keyRestrictedLeftJoins) {
        if (normalQueryMode && isEmpty(keyRestrictedLeftJoins, EnumSet.of(ClauseType.SELECT))) {
            TypedQuery<Object[]> idQuery = em.createQuery(idQueryString, Object[].class);
            if (isCacheable()) {
                jpaProvider.setCacheable(idQuery);
            }
            parameterManager.parameterizeQuery(idQuery);
            return idQuery;
        }

        TypedQuery<Object[]> baseQuery = em.createQuery(idQueryString, Object[].class);
        Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery);

        List<String> keyRestrictedLeftJoinAliases = getKeyRestrictedLeftJoinAliases(baseQuery, keyRestrictedLeftJoins, EnumSet.of(ClauseType.SELECT));
        List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(baseQuery);
        boolean shouldRenderCteNodes = renderCteNodes(false);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(baseQuery, false) : Collections.EMPTY_LIST;
        QuerySpecification querySpecification = new CustomQuerySpecification(
                this, baseQuery, parameterListNames, null, null, keyRestrictedLeftJoinAliases, entityFunctionNodes, mainQuery.cteManager.isRecursive(), ctes, shouldRenderCteNodes
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
        if (normalQueryMode && isEmpty(keyRestrictedLeftJoins, EnumSet.complementOf(EnumSet.of(ClauseType.SELECT, ClauseType.ORDER_BY)))) {
            TypedQuery<T> query = (TypedQuery<T>) em.createQuery(getBaseQueryString(), selectManager.getExpectedQueryResultType());
            if (isCacheable()) {
                jpaProvider.setCacheable(query);
            }
            parameterManager.parameterizeQuery(query, Collections.singleton(ID_PARAM_NAME));
            return applyObjectBuilder(query);
        }

        TypedQuery<T> baseQuery = (TypedQuery<T>) em.createQuery(getBaseQueryString(), selectManager.getExpectedQueryResultType());
        Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery, Collections.singleton(ID_PARAM_NAME));
        parameterListNames.add(ID_PARAM_NAME);

        List<String> keyRestrictedLeftJoinAliases = getKeyRestrictedLeftJoinAliases(baseQuery, keyRestrictedLeftJoins, EnumSet.complementOf(EnumSet.of(ClauseType.SELECT, ClauseType.ORDER_BY)));
        List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(baseQuery);
        boolean shouldRenderCteNodes = renderCteNodes(false);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(baseQuery, false) : Collections.EMPTY_LIST;
        QuerySpecification querySpecification = new CustomQuerySpecification(
                this, baseQuery, parameterListNames, null, null, keyRestrictedLeftJoinAliases, entityFunctionNodes, mainQuery.cteManager.isRecursive(), ctes, shouldRenderCteNodes
        );

        TypedQuery<T> query = new CustomSQLTypedQuery<T>(
                querySpecification,
                baseQuery,
                parameterManager.getTransformers(),
                parameterManager.getValuesParameters(),
                parameterManager.getValuesBinders()
        );

        parameterManager.parameterizeQuery(query, Collections.singleton(ID_PARAM_NAME));

        return applyObjectBuilder(query);
    }

    protected String buildPageCountQueryString(boolean externalRepresentation) {
        StringBuilder sbSelectFrom = new StringBuilder();
        if (externalRepresentation && isMainQuery) {
            mainQuery.cteManager.buildClause(sbSelectFrom);
        }
        buildPageCountQueryString(sbSelectFrom, externalRepresentation);
        return sbSelectFrom.toString();
    }

    private String buildPageCountQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        JoinNode rootNode = joinManager.getRootNodeOrFail("Paginated criteria builders do not support multiple from clause elements!");
        Attribute<?, ?> idAttribute = JpaMetamodelUtils.getIdAttribute(mainQuery.metamodel.entity(rootNode.getType()));
        String idName = idAttribute.getName();
        StringBuilder idClause = new StringBuilder(100);
        rootNode.appendDeReference(idClause, idName);
        // Spaces are important to be able to reuse the string builder without copying
        String countString = jpaProvider.getCustomFunctionInvocation(AbstractCountFunction.FUNCTION_NAME, 1) + "'DISTINCT'," + idClause + ")";
        sbSelectFrom.append("SELECT ").append(countString);

        if (entityId != null) {
            parameterManager.addParameterMapping(ENTITY_PAGE_POSITION_PARAMETER_NAME, entityId, ClauseType.SELECT);

            sbSelectFrom.append(", ");
            sbSelectFrom.append(jpaProvider.getCustomFunctionInvocation(PagePositionFunction.FUNCTION_NAME, 2));

            sbSelectFrom.append('(');
            appendSimplePageIdQueryString(sbSelectFrom);
            sbSelectFrom.append("),");

            sbSelectFrom.append(':').append(ENTITY_PAGE_POSITION_PARAMETER_NAME);
            sbSelectFrom.append(")");
        }

        List<String> whereClauseConjuncts = new ArrayList<>();
        // The count query does not have any fetch owners
        Set<JoinNode> countNodesToFetch = Collections.emptySet();
        // Collect usage of collection join nodes to optimize away the count distinct
        Set<JoinNode> collectionJoinNodes = joinManager.buildClause(sbSelectFrom, EnumSet.of(ClauseType.ORDER_BY, ClauseType.SELECT), null, true, externalRepresentation, whereClauseConjuncts, explicitVersionEntities, countNodesToFetch);
        // TODO: Maybe we can improve this and treat array access joins like non-collection join nodes 
        boolean hasCollectionJoinUsages = collectionJoinNodes.size() > 0;
        
        whereManager.buildClause(sbSelectFrom, whereClauseConjuncts);
        
        // Count distinct is obviously unnecessary if we have no collection joins
        if (!hasCollectionJoinUsages) {
            int idx = sbSelectFrom.indexOf(countString);
            int endIdx = idx + countString.length() - 1;
            String countStar;
            if (jpaProvider.supportsCountStar()) {
                countStar = "COUNT(*";
            } else {
                countStar = jpaProvider.getCustomFunctionInvocation("count_star", 0);
            }
            for (int i = idx, j = 0; i < endIdx; i++, j++) {
                if (j < countStar.length()) {
                    sbSelectFrom.setCharAt(i, countStar.charAt(j));
                } else {
                    sbSelectFrom.setCharAt(i, ' ');
                }
            }
        }

        return sbSelectFrom.toString();
    }

    private String appendSimplePageIdQueryString(StringBuilder sbSelectFrom) {
        queryGenerator.setAliasPrefix(PAGE_POSITION_ID_QUERY_ALIAS_PREFIX);
        
        JoinNode rootNode = joinManager.getRootNodeOrFail("Paginated criteria builders do not support multiple from clause elements!");
        String idName = JpaMetamodelUtils.getIdAttribute(mainQuery.metamodel.entity(rootNode.getType())).getName();
        StringBuilder idClause = new StringBuilder(PAGE_POSITION_ID_QUERY_ALIAS_PREFIX);
        rootNode.appendDeReference(idClause, idName);

        sbSelectFrom.append("SELECT ").append(idClause);
        // TODO: actually we should add the select clauses needed for order bys
        // TODO: if we do so, the page position function has to omit select items other than the first

        List<String> whereClauseConjuncts = new ArrayList<>();
        // The id query does not have any fetch owners
        Set<JoinNode> idNodesToFetch = Collections.emptySet();
        joinManager.buildClause(sbSelectFrom, EnumSet.of(ClauseType.SELECT), PAGE_POSITION_ID_QUERY_ALIAS_PREFIX, false, false, whereClauseConjuncts, explicitVersionEntities, idNodesToFetch);
        whereManager.buildClause(sbSelectFrom, whereClauseConjuncts);

        boolean inverseOrder = false;

        Set<String> clauses = new LinkedHashSet<String>();
        clauses.add(idClause.toString());
        orderByManager.buildGroupByClauses(clauses);
        groupByManager.buildGroupBy(sbSelectFrom, clauses);

        // Resolve select aliases because we might omit the select items
        orderByManager.buildOrderBy(sbSelectFrom, inverseOrder, true);

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
        JoinNode rootNode = joinManager.getRootNodeOrFail("Paginated criteria builders do not support multiple from clause elements!");
        String idName = JpaMetamodelUtils.getIdAttribute(mainQuery.metamodel.entity(rootNode.getType())).getName();
        StringBuilder idClause = new StringBuilder(100);
        rootNode.appendDeReference(idClause, idName);

        // TODO: only append if it does not appear in the order by or it may be included twice
        sbSelectFrom.append("SELECT ").append(idClause);

        if (needsNewIdList) {
            orderByManager.buildSelectClauses(sbSelectFrom, keysetExtraction);
        }

        List<String> whereClauseConjuncts = new ArrayList<>();
        // The id query does not have any fetch owners
        Set<JoinNode> idNodesToFetch = Collections.emptySet();
        joinManager.buildClause(sbSelectFrom, EnumSet.of(ClauseType.SELECT), null, false, externalRepresentation, whereClauseConjuncts, explicitVersionEntities, idNodesToFetch);

        if (keysetMode == KeysetMode.NONE) {
            whereManager.buildClause(sbSelectFrom, whereClauseConjuncts);
        } else {
            sbSelectFrom.append(" WHERE ");

            if (mainQuery.getQueryConfiguration().isOptimizedKeysetPredicateRenderingEnabled()) {
                keysetManager.buildOptimizedKeysetPredicate(sbSelectFrom);
            } else {
                keysetManager.buildKeysetPredicate(sbSelectFrom);
            }

            if (whereManager.hasPredicates() || !whereClauseConjuncts.isEmpty()) {
                sbSelectFrom.append(" AND ");
                whereManager.buildClausePredicate(sbSelectFrom, whereClauseConjuncts);
            }
        }

        boolean inverseOrder = keysetMode == KeysetMode.PREVIOUS;

        Set<String> clauses = new LinkedHashSet<String>();
        clauses.add(idClause.toString());
        orderByManager.buildGroupByClauses(clauses);
        groupByManager.buildGroupBy(sbSelectFrom, clauses);

        // Resolve select aliases to their actual expressions only if the select items aren't included
        orderByManager.buildOrderBy(sbSelectFrom, inverseOrder, !needsNewIdList);

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
        JoinNode rootNode = joinManager.getRootNodeOrFail("Paginated criteria builders do not support multiple from clause elements!");
        String idName = JpaMetamodelUtils.getIdAttribute(mainQuery.metamodel.entity(rootNode.getType())).getName();

        selectManager.buildSelect(sbSelectFrom, false);

        /**
         * we have already selected the IDs so now we only need so select the
         * fields and apply the ordering all other clauses are not required any
         * more and therefore we can also omit any joins which the SELECT or the
         * ORDER_BY clause do not depend on
         */
        List<String> whereClauseConjuncts = new ArrayList<>();
        joinManager.buildClause(sbSelectFrom, EnumSet.complementOf(EnumSet.of(ClauseType.SELECT, ClauseType.ORDER_BY)), null, false, externalRepresentation, whereClauseConjuncts, explicitVersionEntities, nodesToFetch);
        sbSelectFrom.append(" WHERE ");
        rootNode.appendDeReference(sbSelectFrom, idName);
        sbSelectFrom.append(" IN :").append(ID_PARAM_NAME).append("");

        for (String conjunct : whereClauseConjuncts) {
            sbSelectFrom.append(" AND ");
            sbSelectFrom.append(conjunct);
        }

        Set<String> clauses = new LinkedHashSet<String>();
        groupByManager.buildGroupByClauses(clauses);

        int size = transformerGroups.size();
        for (int i = 0; i < size; i++) {
            ExpressionTransformerGroup<?> transformerGroup = transformerGroups.get(i);
            clauses.addAll(transformerGroup.getRequiredGroupByClauses());
        }

        if (hasGroupBy) {
            if (mainQuery.getQueryConfiguration().isImplicitGroupByFromSelectEnabled()) {
                selectManager.buildGroupByClauses(mainQuery.metamodel, clauses);
            }
            if (mainQuery.getQueryConfiguration().isImplicitGroupByFromHavingEnabled()) {
                havingManager.buildGroupByClauses(clauses);
            }
            if (mainQuery.getQueryConfiguration().isImplicitGroupByFromOrderByEnabled()) {
                orderByManager.buildGroupByClauses(clauses);
            }
        }

        if (!clauses.isEmpty()) {
            for (int i = 0; i < size; i++) {
                ExpressionTransformerGroup<?> transformerGroup = transformerGroups.get(i);
                clauses.addAll(transformerGroup.getOptionalGroupByClauses());
            }
        }

        groupByManager.buildGroupBy(sbSelectFrom, clauses);

        havingManager.buildClause(sbSelectFrom);
        queryGenerator.setResolveSelectAliases(false);
        orderByManager.buildOrderBy(sbSelectFrom, false, false);
        queryGenerator.setResolveSelectAliases(true);
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
            orderByManager.buildSelectClauses(sbSelectFrom, true);
        }

        List<String> whereClauseConjuncts = new ArrayList<>();
        joinManager.buildClause(sbSelectFrom, EnumSet.noneOf(ClauseType.class), null, false, externalRepresentation, whereClauseConjuncts, explicitVersionEntities, nodesToFetch);

        if (keysetMode == KeysetMode.NONE) {
            whereManager.buildClause(sbSelectFrom, whereClauseConjuncts);
        } else {
            sbSelectFrom.append(" WHERE ");

            if (mainQuery.getQueryConfiguration().isOptimizedKeysetPredicateRenderingEnabled()) {
                keysetManager.buildOptimizedKeysetPredicate(sbSelectFrom);
            } else {
                keysetManager.buildKeysetPredicate(sbSelectFrom);
            }

            if (whereManager.hasPredicates() || !whereClauseConjuncts.isEmpty()) {
                sbSelectFrom.append(" AND ");
                whereManager.buildClausePredicate(sbSelectFrom, whereClauseConjuncts);
            }
        }

        boolean inverseOrder = keysetMode == KeysetMode.PREVIOUS;

        Set<String> clauses = new LinkedHashSet<String>();
        groupByManager.buildGroupByClauses(clauses);

        int size = transformerGroups.size();
        for (int i = 0; i < size; i++) {
            ExpressionTransformerGroup<?> transformerGroup = transformerGroups.get(i);
            clauses.addAll(transformerGroup.getRequiredGroupByClauses());
        }
        if (hasGroupBy) {
            if (mainQuery.getQueryConfiguration().isImplicitGroupByFromSelectEnabled()) {
                selectManager.buildGroupByClauses(mainQuery.metamodel, clauses);
            }
            if (mainQuery.getQueryConfiguration().isImplicitGroupByFromHavingEnabled()) {
                havingManager.buildGroupByClauses(clauses);
            }
            if (mainQuery.getQueryConfiguration().isImplicitGroupByFromOrderByEnabled()) {
                orderByManager.buildGroupByClauses(clauses);
            }
        }

        if (!clauses.isEmpty()) {
            for (int i = 0; i < size; i++) {
                ExpressionTransformerGroup<?> transformerGroup = transformerGroups.get(i);
                clauses.addAll(transformerGroup.getOptionalGroupByClauses());
            }
        }

        groupByManager.buildGroupBy(sbSelectFrom, clauses);

        havingManager.buildClause(sbSelectFrom);

        orderByManager.buildOrderBy(sbSelectFrom, inverseOrder, false);

        // execute illegal collection access check
        orderByManager.acceptVisitor(new IllegalSubqueryDetector(aliasManager));

        return sbSelectFrom.toString();
    }

    @Override
    public PaginatedCriteriaBuilder<T> distinct() {
        throw new IllegalStateException("Calling distinct() on a PaginatedCriteriaBuilder is not allowed.");
    }

    @Override
    public PaginatedCriteriaBuilder<T> groupBy(String... paths) {
        throw new IllegalStateException("Calling groupBy() on a PaginatedCriteriaBuilder is not allowed.");
    }

    @Override
    public PaginatedCriteriaBuilder<T> groupBy(String expression) {
        throw new IllegalStateException("Calling groupBy() on a PaginatedCriteriaBuilder is not allowed.");
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
