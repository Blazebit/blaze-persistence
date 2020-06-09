/*
 * Copyright 2014 - 2020 Blazebit.
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
import com.blazebit.persistence.ConfigurationProperties;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.HavingOrBuilder;
import com.blazebit.persistence.Keyset;
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
import com.blazebit.persistence.impl.builder.object.CountExtractionObjectBuilder;
import com.blazebit.persistence.impl.builder.object.DelegatingKeysetExtractionObjectBuilder;
import com.blazebit.persistence.impl.builder.object.KeysetExtractionObjectBuilder;
import com.blazebit.persistence.impl.function.alias.AliasFunction;
import com.blazebit.persistence.impl.function.coltrunc.ColumnTruncFunction;
import com.blazebit.persistence.impl.function.entity.EntityFunction;
import com.blazebit.persistence.impl.function.limit.LimitFunction;
import com.blazebit.persistence.impl.function.pageposition.PagePositionFunction;
import com.blazebit.persistence.impl.function.querywrapper.QueryWrapperFunction;
import com.blazebit.persistence.impl.function.rowvalue.RowValueSubqueryComparisonFunction;
import com.blazebit.persistence.impl.keyset.KeysetMode;
import com.blazebit.persistence.impl.keyset.KeysetPaginationHelper;
import com.blazebit.persistence.impl.keyset.SimpleKeysetLink;
import com.blazebit.persistence.impl.query.CTENode;
import com.blazebit.persistence.impl.query.CustomQuerySpecification;
import com.blazebit.persistence.impl.query.CustomSQLTypedQuery;
import com.blazebit.persistence.impl.query.EntityFunctionNode;
import com.blazebit.persistence.impl.query.ObjectBuilderTypedQuery;
import com.blazebit.persistence.impl.query.QuerySpecification;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.spi.AttributeAccessor;

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
    private static final Set<ClauseType> OBJECT_QUERY_CLAUSE_EXCLUSIONS = EnumSet.complementOf(EnumSet.of(ClauseType.ORDER_BY, ClauseType.SELECT));

    private boolean keysetExtraction;
    private boolean withExtractAllKeysets = false;
    private boolean withCountQuery = true;
    private boolean withForceIdQuery = false;
    private Boolean withInlineIdQuery;
    private boolean withInlineCountQuery;
    private long maximumCount = Long.MAX_VALUE;
    private int highestOffset = 0;
    private final KeysetPage keysetPage;
    private final ResolvedExpression[] identifierExpressions;

    // Mutable state
    private final Object entityId;
    private boolean needsNewIdList;
    private int[] keysetToSelectIndexMapping;
    private String[] identifierToUseSelectAliases;
    private KeysetMode keysetMode;
    private boolean forceFirstResult;

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
        if (mainQuery.getQueryConfiguration().getInlineCountQueryEnabled() == null) {
            this.withInlineCountQuery = entityId == null && mainQuery.jpaProvider.supportsSubqueryAliasShadowing();
        } else {
            this.withInlineCountQuery = mainQuery.getQueryConfiguration().getInlineCountQueryEnabled();
        }
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
        if (mainQuery.getQueryConfiguration().getInlineCountQueryEnabled() == null) {
            this.withInlineCountQuery = mainQuery.jpaProvider.supportsSubqueryAliasShadowing();
        } else {
            this.withInlineCountQuery = mainQuery.getQueryConfiguration().getInlineCountQueryEnabled();
        }
        updateKeysetMode();
    }

    @Override
    public <Y> PaginatedCriteriaBuilder<Y> copy(Class<Y> resultClass) {
        FullQueryBuilder<Y, ?> criteriaBuilder = super.copy(resultClass);
        PaginatedCriteriaBuilder<Y> builder;
        if (entityId != null) {
            builder = criteriaBuilder.pageAndNavigate(entityId, maxResults);
        } else if (keysetPage != null) {
            builder = criteriaBuilder.page(keysetPage, firstResult, maxResults);
        } else {
            builder = criteriaBuilder.page(firstResult, maxResults);
        }

        builder.withKeysetExtraction(keysetExtraction);
        builder.withExtractAllKeysets(withExtractAllKeysets);
        builder.withCountQuery(withCountQuery);
        builder.withForceIdQuery(withForceIdQuery);
        builder.withHighestKeysetOffset(highestOffset);
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

    @Override
    public PaginatedCriteriaBuilder<T> afterKeyset(Keyset keyset) {
        super.afterKeyset(keyset);
        this.keysetMode = KeysetMode.NEXT;
        this.forceFirstResult = true;
        return this;
    }

    @Override
    public PaginatedCriteriaBuilder<T> beforeKeyset(Keyset keyset) {
        super.beforeKeyset(keyset);
        this.keysetMode = KeysetMode.PREVIOUS;
        this.forceFirstResult = true;
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
        if (!keysetExtraction) {
            this.withExtractAllKeysets = false;
        }
        return this;
    }

    @Override
    public boolean isKeysetExtraction() {
        return keysetExtraction;
    }

    @Override
    public PaginatedCriteriaBuilder<T> withExtractAllKeysets(boolean withExtractAllKeysets) {
        this.withExtractAllKeysets = withExtractAllKeysets;
        if (withExtractAllKeysets) {
            this.keysetExtraction = true;
        }
        return this;
    }

    @Override
    public boolean isWithExtractAllKeysets() {
        return withExtractAllKeysets;
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
    public PaginatedCriteriaBuilder<T> withBoundedCount(long maximumCount) {
        if (this.maximumCount != maximumCount) {
            this.maximumCount = maximumCount;
            prepareForModification(ClauseType.SELECT);
        }
        return this;
    }

    @Override
    public long getBoundedCount() {
        return maximumCount;
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
    public PaginatedCriteriaBuilder<T> withInlineIdQuery(boolean withInlineIdQuery) {
        if (withInlineIdQuery) {
            if (!mainQuery.jpaProvider.supportsSubqueryInFunction()) {
                throw new IllegalStateException("Can't inline the id query because the JPA provider does not support subqueries in functions");
            } else if (!mainQuery.jpaProvider.supportsSubqueryAliasShadowing()) {
                throw new IllegalStateException("Can't inline the id query because the JPA provider does not support subquery alias shadowing!");
            } else if ((!mainQuery.dbmsDialect.supportsRowValueConstructor() || !mainQuery.jpaProvider.supportsNonScalarSubquery()) && getIdentifierExpressionsToUse().length != 1) {
                if (!mainQuery.jpaProvider.supportsNonScalarSubquery()) {
                    throw new IllegalStateException("Can't inline the id query because pagination is based on multiple identifier expressions but the JPA provider does not support non-scalar subqueries!");
                } else {
                    throw new IllegalStateException("Can't inline the id query because pagination is based on multiple identifier expressions but the database does not support the row value constructor syntax!");
                }
            }
        }
        if (this.withInlineIdQuery != null && this.withInlineIdQuery != withInlineIdQuery) {
            prepareForModification(ClauseType.SELECT);
        }
        this.withInlineIdQuery = withInlineIdQuery;
        return this;
    }

    @Override
    public boolean isWithInlineIdQuery() {
        if (withInlineIdQuery == null) {
            // To support EclipseLink, we first need a way to get around the subquery alias shadowing issue i.e. an automatic renaming
            // TODO: we could emulate the LIMIT function with window functions to get around jpaProvider.supportsSubqueryInFunction() for EclipseLink
            // TODO: we could emulate row value constructor support by using an EXISTS predicate and a nested subquery for Oracle and MSSQL and EclipseLink
            if (mainQuery.getQueryConfiguration().getInlineIdQueryEnabled() == null) {
                withInlineIdQuery = mainQuery.jpaProvider.supportsSubqueryInFunction() && mainQuery.jpaProvider.supportsSubqueryAliasShadowing()
                        && (getIdentifierExpressionsToUse().length == 1 || mainQuery.dbmsDialect.supportsRowValueConstructor() && mainQuery.jpaProvider.supportsNonScalarSubquery());
            } else {
                withInlineIdQuery = mainQuery.getQueryConfiguration().getInlineIdQueryEnabled();
            }
        }
        return withInlineIdQuery;
    }

    @Override
    public boolean isWithInlineCountQuery() {
        return withInlineCountQuery;
    }

    @Override
    public PaginatedCriteriaBuilder<T> withInlineCountQuery(boolean withInlineCountQuery) {
        if (withInlineCountQuery) {
            if (entityId != null) {
                throw new IllegalStateException("Can't inline the count query when paginating to a page by entity id!");
            } else if (!mainQuery.jpaProvider.supportsSubqueryAliasShadowing()) {
                throw new IllegalStateException("Can't inline the count query because the JPA provider does not support subquery alias shadowing!");
            }
        }
        if (this.withInlineCountQuery != withInlineCountQuery) {
            prepareForModification(ClauseType.SELECT);
        }
        this.withInlineCountQuery = withInlineCountQuery;
        return this;
    }

    @Override
    public PaginatedCriteriaBuilder<T> setProperty(String propertyName, String propertyValue) {
        super.setProperty(propertyName, propertyValue);
        Boolean enabled;
        switch (propertyName) {
            case ConfigurationProperties.INLINE_ID_QUERY:
                enabled = mainQuery.getQueryConfiguration().getInlineIdQueryEnabled();
                if (enabled != null) {
                    withInlineIdQuery(enabled);
                }
                break;
            case ConfigurationProperties.INLINE_COUNT_QUERY:
                enabled = mainQuery.getQueryConfiguration().getInlineCountQueryEnabled();
                if (enabled != null) {
                    withInlineCountQuery(enabled);
                }
                break;
            default:
                break;
        }
        return this;
    }

    @Override
    public PaginatedCriteriaBuilder<T> setProperties(Map<String, String> properties) {
        super.setProperties(properties);
        Boolean enabled;
        if (properties.containsKey(ConfigurationProperties.INLINE_ID_QUERY)) {
            enabled = mainQuery.getQueryConfiguration().getInlineIdQueryEnabled();
            if (enabled != null) {
                withInlineIdQuery(enabled);
            }
        }
        if (properties.containsKey(ConfigurationProperties.INLINE_COUNT_QUERY)) {
            enabled = mainQuery.getQueryConfiguration().getInlineCountQueryEnabled();
            if (enabled != null) {
                withInlineCountQuery(enabled);
            }
        }
        return this;
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

    @Override
    public CriteriaBuilder<Object[]> createPageIdQuery() {
        return createPageIdQuery(keysetPage, firstResult, maxResults, getIdentifierExpressionsToUse());
    }

    private <X> TypedQuery<X> getCountQuery(String countQueryString, Class<X> resultType, boolean normalQueryMode, Set<JoinNode> keyRestrictedLeftJoins, List<JoinNode> entityFunctions, JoinNode dualNode) {
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
        List<EntityFunctionNode> entityFunctionNodes;
        if (dualNode == null) {
            entityFunctionNodes = getEntityFunctionNodes(baseQuery, entityFunctions);
        } else {
            entityFunctionNodes = getEntityFunctionNodes(baseQuery, entityFunctions, Collections.<JoinNode>emptyList(), false);
        }
        boolean shouldRenderCteNodes = renderCteNodes(false);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(false) : Collections.EMPTY_LIST;
        Set<Parameter<?>> parameters = parameterManager.getParameters();
        Map<String, String> valuesParameters = parameterManager.getValuesParameters();
        Map<String, ValuesParameterBinder> valuesBinders = parameterManager.getValuesBinders();
        if (dualNode != null) {
            String valueParameterName = dualNode.getAlias() + "_value_0";
            String[][] parameterNames = new String[1][1];
            parameterNames[0][0] = valueParameterName;
            ParameterManager.ValuesParameterWrapper valuesParameterWrapper = new ParameterManager.ValuesParameterWrapper(dualNode.getJavaType(), parameterNames, new AttributeAccessor[1]);
            parameters.add(new ParameterManager.ParameterImpl<Object>(dualNode.getAlias(), false, null, null, valuesParameterWrapper));
            valuesParameters = new HashMap<>(valuesParameters);
            valuesParameters.put(valueParameterName, dualNode.getAlias());
            valuesBinders.put(dualNode.getAlias(), valuesParameterWrapper.getBinder());
        }
        QuerySpecification querySpecification = new CustomQuerySpecification(
                this, baseQuery, parameters, parameterListNames, null, null, keyRestrictedLeftJoinAliases, entityFunctionNodes,
                mainQuery.cteManager.isRecursive(), ctes, shouldRenderCteNodes, mainQuery.getQueryConfiguration().isQueryPlanCacheEnabled()
        );

        TypedQuery<X> countQuery = new CustomSQLTypedQuery<X>(
                querySpecification,
                baseQuery,
                parameterManager.getTransformers(),
                valuesParameters,
                valuesBinders
        );

        if (dualNode == null) {
            parameterManager.parameterizeQuery(countQuery);
        } else {
            parameterManager.parameterizeQuery(countQuery, dualNode.getAlias());
            countQuery.setParameter(dualNode.getAlias(), Collections.singleton(0L));
        }
        return countQuery;
    }

    @Override
    public PaginatedTypedQueryImpl<T> getQuery() {
        prepareAndCheck();
        // We can only use the query directly if we have no ctes, entity functions or hibernate bugs
        Set<JoinNode> keyRestrictedLeftJoins = getKeyRestrictedLeftJoins();
        boolean normalQueryMode = !isMainQuery || (!mainQuery.cteManager.hasCtes() && !joinManager.hasEntityFunctions() && keyRestrictedLeftJoins.isEmpty());
        TypedQuery<?> countQuery;
        String countQueryString = getPageCountQueryStringWithoutCheck();
        List<JoinNode> entityFunctions = null;
        Set<JoinNode> alwaysIncludedNodes = null;
        if (!normalQueryMode) {
            alwaysIncludedNodes = getIdentifierExpressionsToUseNonRootJoinNodes();
            entityFunctions = joinManager.getEntityFunctions(COUNT_QUERY_GROUP_BY_CLAUSE_EXCLUSIONS, true, alwaysIncludedNodes);
        }

        if (maximumCount == Long.MAX_VALUE) {
            if (entityId == null) {
                // No reference entity id, so just do a simple count query
                countQuery = getCountQuery(countQueryString, Long.class, normalQueryMode, keyRestrictedLeftJoins, entityFunctions, null);
            } else {
                countQuery = getCountQuery(countQueryString, Object[].class, normalQueryMode, keyRestrictedLeftJoins, entityFunctions, null);
            }
        } else {
            List<JoinNode> countEntityFunctions = new ArrayList<>();
            JoinNode valuesNode = createDualNode();
            countEntityFunctions.add(valuesNode);
            if (entityId == null) {
                // No reference entity id, so just do a simple count query
                countQuery = getCountQuery(countQueryString, Long.class, false, Collections.<JoinNode>emptySet(), countEntityFunctions, valuesNode);
            } else {
                countQuery = getCountQuery(countQueryString, Object[].class, false, Collections.<JoinNode>emptySet(), countEntityFunctions, valuesNode);
            }
        }

        TypedQuery<?> idQuery = null;
        TypedQuery<T> objectQuery;
        ObjectBuilder<T> objectBuilder;
        boolean inlinedIdQuery;
        boolean inlinedCountQuery = firstResult < maximumCount && withCountQuery && withInlineCountQuery;
        if (!isWithInlineIdQuery() && (hasCollections || withForceIdQuery)) {
            String idQueryString = getPageIdQueryStringWithoutCheck();
            if (normalQueryMode) {
                entityFunctions = Collections.emptyList();
            } else {
                entityFunctions = joinManager.getEntityFunctions(ID_QUERY_GROUP_BY_CLAUSE_EXCLUSIONS, true, alwaysIncludedNodes);
            }
            idQuery = getIdQuery(idQueryString, normalQueryMode, keyRestrictedLeftJoins, entityFunctions);
            if (normalQueryMode) {
                entityFunctions = Collections.emptyList();
            } else {
                entityFunctions = joinManager.getEntityFunctions(OBJECT_QUERY_CLAUSE_EXCLUSIONS, false, alwaysIncludedNodes);
            }
            objectQuery = getObjectQueryById(normalQueryMode, keyRestrictedLeftJoins, entityFunctions);
            objectBuilder = null;
            inlinedIdQuery = false;
        } else {
            if (normalQueryMode) {
                entityFunctions = Collections.emptyList();
            } else {
                entityFunctions = joinManager.getEntityFunctions(hasGroupBy ? NO_CLAUSE_EXCLUSION : OBJECT_QUERY_WITHOUT_GROUP_BY_EXCLUSIONS, false, alwaysIncludedNodes);
            }
            Map.Entry<TypedQuery<T>, ObjectBuilder<T>> entry = getObjectQuery(normalQueryMode, keyRestrictedLeftJoins, entityFunctions);
            objectQuery = entry.getKey();
            objectBuilder = entry.getValue();
            inlinedIdQuery = isWithInlineIdQuery() && (hasCollections || withForceIdQuery);
        }
        PaginatedTypedQueryImpl<T> query = new PaginatedTypedQueryImpl<>(
                withExtractAllKeysets,
                firstResult < maximumCount && withCountQuery,
                maximumCount != Long.MAX_VALUE,
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
                keysetPage,
                forceFirstResult,
                inlinedIdQuery,
                inlinedCountQuery
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
        Set<JoinNode> keyRestrictedLeftJoins = getKeyRestrictedLeftJoins();
        Set<JoinNode> alwaysIncludedNodes = getIdentifierExpressionsToUseNonRootJoinNodes();
        boolean normalQueryMode = !isMainQuery || (!mainQuery.cteManager.hasCtes() && !joinManager.hasEntityFunctions() && keyRestrictedLeftJoins.isEmpty());
        String countQueryString = getPageCountQueryStringWithoutCheck();
        List<JoinNode> entityFunctions = joinManager.getEntityFunctions(COUNT_QUERY_GROUP_BY_CLAUSE_EXCLUSIONS, true, alwaysIncludedNodes);
        return getCountQuery(countQueryString, Long.class, normalQueryMode, keyRestrictedLeftJoins, entityFunctions, null);
    }

    @Override
    public String getPageCountQueryString() {
        prepareAndCheck();
        return getExternalPageCountQueryString();
    }

    private String getPageCountQueryStringWithoutCheck() {
        if (cachedCountQueryString == null) {
            cachedCountQueryString = buildPageCountQueryString(false, false, maximumCount);
        }

        return cachedCountQueryString;
    }

    protected String getExternalPageCountQueryString() {
        if (cachedExternalCountQueryString == null) {
            cachedExternalCountQueryString = buildPageCountQueryString(true, false, maximumCount);
        }

        return cachedExternalCountQueryString;
    }

    @Override
    public String getPageIdQueryString() {
        prepareAndCheck();
        return getExternalPageIdQueryString();
    }

    private String getPageIdQueryStringWithoutCheck() {
        if (cachedIdQueryString == null && !isWithInlineIdQuery() && (hasCollections || withForceIdQuery)) {
            cachedIdQueryString = buildPageIdQueryString(false);
        }

        return cachedIdQueryString;
    }

    protected String getExternalPageIdQueryString() {
        if (cachedExternalIdQueryString == null && !isWithInlineIdQuery() && (hasCollections || withForceIdQuery)) {
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
    protected String getBaseQueryString(StringBuilder lateralSb, JoinNode lateralJoinNode) {
        if (cachedQueryString == null) {
            if (!isWithInlineIdQuery() && (hasCollections || withForceIdQuery)) {
                cachedQueryString = buildBaseQueryString(false);
            } else {
                cachedQueryString = buildObjectQueryString(false);
            }
        }

        return cachedQueryString;
    }

    protected String getExternalQueryString() {
        if (cachedExternalQueryString == null) {
            if (!isWithInlineIdQuery() && (hasCollections || withForceIdQuery)) {
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
        prepareAndCheckCtes();
        prepareSelect();
        if (!orderByManager.hasOrderBys()) {
            throw new IllegalStateException("Pagination requires at least one order by item!");
        }

        JoinVisitor joinVisitor = applyImplicitJoins(null);
        // We pass true here to always generate implicit group bys for select and order by clauses. We filter these out later if necessary
        applyExpressionTransformersAndBuildGroupByClauses(true, joinVisitor);
        analyzeConstantifiedJoinNodes();
        hasCollections = joinManager.hasCollections();

        if (joinManager.hasFullJoin()) {
            throw new IllegalStateException("Cannot paginate with full outer joins!");
        }

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
        List<OrderByExpression> orderByExpressions = orderByManager.getOrderByExpressions(false, whereManager.rootPredicate.getPredicate(), hasGroupBy ? Arrays.asList(getIdentifierExpressions()) : Collections.<ResolvedExpression>emptyList(), joinVisitor);
        if (!orderByExpressions.get(orderByExpressions.size() - 1).isResultUnique()) {
            throw new IllegalStateException("The order by items of the query builder are not guaranteed to produce unique tuples! Consider also ordering by the entity identifier!");
        }

        if (keysetManager.hasKeyset()) {
            keysetManager.initialize(orderByExpressions);
        }

        // initialize index mappings that we use to avoid putting keyset expressions into select clauses multiple times
        if (!isWithInlineIdQuery() && (hasCollections || withForceIdQuery)) {
            initializeOrderByAliasesWithIdentifierToUse(orderByExpressions);
        } else if (keysetExtraction || withInlineCountQuery) {
            if (isWithInlineIdQuery()) {
                initializeOrderByAliasesWithIdentifierToUse(orderByExpressions);
                // If we have no select item, this means we implicitly select the root and thus need to offset the index mapping as we will append that
                if (selectManager.getSelectInfos().size() == 0) {
                    for (int j = 0; j < keysetToSelectIndexMapping.length; j++) {
                        if (keysetToSelectIndexMapping[j] != -1) {
                            keysetToSelectIndexMapping[j] = keysetToSelectIndexMapping[j] + 1;
                        }
                    }
                }
            } else {
                identifierToUseSelectAliases = null;
            }
            List<SelectInfo> selectInfos = selectManager.getSelectInfos();
            if (selectInfos.size() == 0) {
                if (keysetToSelectIndexMapping == null) {
                    keysetToSelectIndexMapping = new int[orderByExpressions.size()];
                    Arrays.fill(keysetToSelectIndexMapping, -1);
                }
            } else {
                Map<String, Integer> selectExpressionStringMap = new HashMap<>(selectInfos.size() * 2);
                for (int i = 0; i < selectInfos.size(); i++) {
                    SelectInfo selectInfo = selectInfos.get(i);
                    selectExpressionStringMap.put(selectInfo.getExpression().toString(), i);
                    if (selectInfo.getAlias() != null) {
                        selectExpressionStringMap.put(selectInfo.getAlias(), i);
                    }
                }

                keysetToSelectIndexMapping = new int[orderByExpressions.size()];

                Integer index;
                for (int i = 0; i < orderByExpressions.size(); i++) {
                    index = selectExpressionStringMap.get(orderByExpressions.get(i).getExpression().toString());
                    keysetToSelectIndexMapping[i] = index == null ? -1 : index;
                }
            }
        } else {
            keysetToSelectIndexMapping = null;
            if (isWithInlineIdQuery()) {
                initializeOrderByAliasesWithIdentifierToUse(orderByExpressions);
            } else {
                identifierToUseSelectAliases = null;
            }
        }

        // When we do keyset extraction of have complex order bys, we have to append additional expression to the end of the select clause which have to be removed later
        needsNewIdList = keysetExtraction
                || orderByManager.hasComplexOrderBys();

        // No need to do the check again if no mutation occurs
        needsCheck = false;
    }

    private void initializeOrderByAliasesWithIdentifierToUse(List<OrderByExpression> orderByExpressions) {
        ResolvedExpression[] identifierExpressionsToUse = getIdentifierExpressionsToUse();
        Map<String, Integer> identifierExpressionStringMap = new HashMap<>(identifierExpressionsToUse.length);

        for (int i = 0; i < identifierExpressionsToUse.length; i++) {
            identifierExpressionStringMap.put(identifierExpressionsToUse[i].getExpressionString(), i);
        }

        keysetToSelectIndexMapping = new int[orderByExpressions.size()];
        identifierToUseSelectAliases = new String[identifierExpressionsToUse.length];

        Integer index;
        for (int i = 0; i < orderByExpressions.size(); i++) {
            String potentialSelectAlias = null;
            AliasInfo aliasInfo = null;
            if (orderByExpressions.get(i).getExpression() instanceof PathExpression) {
                potentialSelectAlias = orderByExpressions.get(i).getExpression().toString();
                aliasInfo = aliasManager.getAliasInfo(potentialSelectAlias);
            }
            if (aliasInfo instanceof SelectInfo) {
                index = identifierExpressionStringMap.get(((SelectInfo) aliasInfo).getExpression().toString());
                if (index == null) {
                    keysetToSelectIndexMapping[i] = -1;
                } else {
                    identifierToUseSelectAliases[i] = potentialSelectAlias;
                    keysetToSelectIndexMapping[i] = index;
                }
            } else if (keysetExtraction || withInlineCountQuery) {
                index = identifierExpressionStringMap.get(potentialSelectAlias);
                keysetToSelectIndexMapping[i] = index == null ? -1 : index;
            }
        }
        if (!keysetExtraction && !withInlineCountQuery) {
            keysetToSelectIndexMapping = null;
        }
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
    private Map.Entry<TypedQuery<T>, ObjectBuilder<T>> getObjectQuery(boolean normalQueryMode, Set<JoinNode> keyRestrictedLeftJoins, List<JoinNode> entityFunctions) {
        String queryString = getBaseQueryString(null, null);
        Class<?> expectedResultType;

        // When the keyset is included the query obviously produces an array
        if (keysetExtraction || firstResult < maximumCount && withCountQuery && withInlineCountQuery) {
            expectedResultType = Object[].class;
        } else {
            expectedResultType = selectManager.getExpectedQueryResultType();
        }

        Set<ClauseType> clauseExclusions;
        if (isWithInlineIdQuery() && (hasCollections || withForceIdQuery)) {
            clauseExclusions = OBJECT_QUERY_CLAUSE_EXCLUSIONS;
        } else if (hasGroupBy) {
            clauseExclusions = NO_CLAUSE_EXCLUSION;
        } else {
            clauseExclusions = OBJECT_QUERY_WITHOUT_GROUP_BY_EXCLUSIONS;
        }
        TypedQuery<T> query;

        if (normalQueryMode && isEmpty(keyRestrictedLeftJoins, clauseExclusions)) {
            query = (TypedQuery<T>) em.createQuery(queryString, expectedResultType);
            if (isCacheable()) {
                mainQuery.jpaProvider.setCacheable(query);
            }
            boolean externalIdQuery = !isWithInlineIdQuery() && (hasCollections || withForceIdQuery);
            if (!externalIdQuery && firstResult < maximumCount && withCountQuery && withInlineCountQuery && maximumCount != Long.MAX_VALUE) {
                parameterManager.parameterizeQuery(query, getDualNodeAlias());
                query.setParameter(getDualNodeAlias() + "_value_0", 0L);
            } else {
                parameterManager.parameterizeQuery(query);
            }
        } else {
            TypedQuery<T> baseQuery = (TypedQuery<T>) em.createQuery(queryString, expectedResultType);
            Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery);

            List<String> keyRestrictedLeftJoinAliases = getKeyRestrictedLeftJoinAliases(baseQuery, keyRestrictedLeftJoins, clauseExclusions);
            List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(baseQuery, entityFunctions);
            boolean shouldRenderCteNodes = renderCteNodes(false);
            List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(false) : Collections.EMPTY_LIST;

            Set<Parameter<?>> parameters = parameterManager.getParameters();
            Map<String, String> valuesParameters = parameterManager.getValuesParameters();
            Map<String, ValuesParameterBinder> valuesBinders = parameterManager.getValuesBinders();

            boolean externalIdQuery = !isWithInlineIdQuery() && (hasCollections || withForceIdQuery);
            JoinNode dualNode = null;
            if (!externalIdQuery && firstResult < maximumCount && withCountQuery && withInlineCountQuery && maximumCount != Long.MAX_VALUE) {
                dualNode = createDualNode();
                String valueParameterName = dualNode.getAlias() + "_value_0";
                String[][] parameterNames = new String[1][1];
                parameterNames[0][0] = valueParameterName;
                ParameterManager.ValuesParameterWrapper valuesParameterWrapper = new ParameterManager.ValuesParameterWrapper(dualNode.getJavaType(), parameterNames, new AttributeAccessor[1]);
                parameters.add(new ParameterManager.ParameterImpl<Object>(dualNode.getAlias(), false, null, null, valuesParameterWrapper));
                valuesParameters = new HashMap<>(valuesParameters);
                valuesParameters.put(valueParameterName, dualNode.getAlias());
                valuesBinders.put(dualNode.getAlias(), valuesParameterWrapper.getBinder());
            }

            QuerySpecification querySpecification = new CustomQuerySpecification(
                    this, baseQuery, parameters, parameterListNames, null, null, keyRestrictedLeftJoinAliases, entityFunctionNodes,
                    mainQuery.cteManager.isRecursive(), ctes, shouldRenderCteNodes, mainQuery.getQueryConfiguration().isQueryPlanCacheEnabled()
            );

            query = new CustomSQLTypedQuery<T>(
                    querySpecification,
                    baseQuery,
                    parameterManager.getTransformers(),
                    valuesParameters,
                    valuesBinders
            );

            if (dualNode == null) {
                parameterManager.parameterizeQuery(query);
            } else {
                parameterManager.parameterizeQuery(query, dualNode.getAlias());
                query.setParameter(dualNode.getAlias(), Collections.singleton(0L));
            }
        }

        ObjectBuilder<T> objectBuilder = null;
        ObjectBuilder<T> transformerObjectBuilder = selectManager.getSelectObjectBuilder();
        boolean inlinedCountQuery = firstResult < maximumCount && withCountQuery && withInlineCountQuery;

        if (keysetExtraction) {
            if (transformerObjectBuilder == null) {
                objectBuilder = new KeysetExtractionObjectBuilder<T>(keysetToSelectIndexMapping, keysetMode, maxResults, highestOffset, selectManager.getExpectedQueryResultType() != Object[].class, withExtractAllKeysets, inlinedCountQuery);
            } else {
                objectBuilder = new DelegatingKeysetExtractionObjectBuilder<T>(transformerObjectBuilder, keysetToSelectIndexMapping, keysetMode, maxResults, highestOffset, withExtractAllKeysets, inlinedCountQuery);
            }

            transformerObjectBuilder = objectBuilder;
        } else if (inlinedCountQuery && transformerObjectBuilder != null) {
            transformerObjectBuilder = objectBuilder = new CountExtractionObjectBuilder<>(transformerObjectBuilder);
        }

        if (transformerObjectBuilder != null) {
            query = new ObjectBuilderTypedQuery<>(query, transformerObjectBuilder);
        }

        return new AbstractMap.SimpleEntry<TypedQuery<T>, ObjectBuilder<T>>(query, objectBuilder);
    }

    private TypedQuery<Object[]> getIdQuery(String idQueryString, boolean normalQueryMode, Set<JoinNode> keyRestrictedLeftJoins, List<JoinNode> entityFunctions) {
        if (normalQueryMode && isEmpty(keyRestrictedLeftJoins, ID_QUERY_CLAUSE_EXCLUSIONS)) {
            TypedQuery<Object[]> idQuery = em.createQuery(idQueryString, Object[].class);
            if (isCacheable()) {
                mainQuery.jpaProvider.setCacheable(idQuery);
            }
            if (firstResult < maximumCount && withCountQuery && withInlineCountQuery && maximumCount != Long.MAX_VALUE) {
                parameterManager.parameterizeQuery(idQuery, getDualNodeAlias());
                idQuery.setParameter(getDualNodeAlias() + "_value_0", 0L);
            } else {
                parameterManager.parameterizeQuery(idQuery);
            }
            return idQuery;
        }

        TypedQuery<Object[]> baseQuery = em.createQuery(idQueryString, Object[].class);
        Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery);

        List<String> keyRestrictedLeftJoinAliases = getKeyRestrictedLeftJoinAliases(baseQuery, keyRestrictedLeftJoins, ID_QUERY_CLAUSE_EXCLUSIONS);
        List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(baseQuery, entityFunctions);
        boolean shouldRenderCteNodes = renderCteNodes(false);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(false) : Collections.EMPTY_LIST;
        QuerySpecification querySpecification = new CustomQuerySpecification(
                this, baseQuery, parameterManager.getParameters(), parameterListNames, null, null, keyRestrictedLeftJoinAliases, entityFunctionNodes,
                mainQuery.cteManager.isRecursive(), ctes, shouldRenderCteNodes, mainQuery.getQueryConfiguration().isQueryPlanCacheEnabled()
        );

        TypedQuery<Object[]> idQuery = new CustomSQLTypedQuery<Object[]>(
                querySpecification,
                baseQuery,
                parameterManager.getTransformers(),
                parameterManager.getValuesParameters(),
                parameterManager.getValuesBinders()
        );

        if (firstResult < maximumCount && withCountQuery && withInlineCountQuery && maximumCount != Long.MAX_VALUE) {
            parameterManager.parameterizeQuery(idQuery, getDualNodeAlias());
            idQuery.setParameter(getDualNodeAlias() + "_value_0", 0L);
        } else {
            parameterManager.parameterizeQuery(idQuery);
        }
        return idQuery;
    }

    @SuppressWarnings("unchecked")
    private TypedQuery<T> getObjectQueryById(boolean normalQueryMode, Set<JoinNode> keyRestrictedLeftJoins, List<JoinNode> entityFunctions) {
        ResolvedExpression[] identifierExpressionsToUse = getIdentifierExpressionsToUse();
        String skippedParameterPrefix = identifierExpressionsToUse.length == 1 ? ID_PARAM_NAME : ID_PARAM_NAME + "_";
        if (normalQueryMode && isEmpty(keyRestrictedLeftJoins, OBJECT_QUERY_CLAUSE_EXCLUSIONS)) {
            TypedQuery<T> query = (TypedQuery<T>) em.createQuery(getBaseQueryString(null, null), selectManager.getExpectedQueryResultType());
            if (isCacheable()) {
                mainQuery.jpaProvider.setCacheable(query);
            }
            parameterManager.parameterizeQuery(query, skippedParameterPrefix);
            return applyObjectBuilder(query);
        }

        TypedQuery<T> baseQuery = (TypedQuery<T>) em.createQuery(getBaseQueryString(null, null), selectManager.getExpectedQueryResultType());
        Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery, ID_PARAM_NAME);

        if (identifierExpressionsToUse.length == 1) {
            parameterListNames.add(ID_PARAM_NAME);
        }

        List<String> keyRestrictedLeftJoinAliases = getKeyRestrictedLeftJoinAliases(baseQuery, keyRestrictedLeftJoins, OBJECT_QUERY_CLAUSE_EXCLUSIONS);
        List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(baseQuery, entityFunctions);
        boolean shouldRenderCteNodes = renderCteNodes(false);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(false) : Collections.EMPTY_LIST;
        Set<Parameter<?>> parameters = new HashSet<>(parameterManager.getParameters());
        if (identifierExpressionsToUse.length == 1) {
            parameters.add(baseQuery.getParameter(ID_PARAM_NAME));
        }
        QuerySpecification querySpecification = new CustomQuerySpecification(
                this, baseQuery, parameters, parameterListNames, null, null, keyRestrictedLeftJoinAliases, entityFunctionNodes,
                mainQuery.cteManager.isRecursive(), ctes, shouldRenderCteNodes, mainQuery.getQueryConfiguration().isQueryPlanCacheEnabled()
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
        appendIdentifierExpressions(sbSelectFrom, false);

        // TODO: actually we should add the select clauses needed for order bys
        // TODO: if we do so, the page position function has to omit select items other than the first

        List<String> whereClauseConjuncts = new ArrayList<>();
        List<String> optionalWhereClauseConjuncts = new ArrayList<>();
        // The id query does not have any fetch owners
        // Note that we always exclude the nodes with group by dependency. We consider just the ones from the identifiers
        Set<JoinNode> idNodesToFetch = Collections.emptySet();
        Set<JoinNode> identifierExpressionsToUseNonRootJoinNodes = getIdentifierExpressionsToUseNonRootJoinNodes();
        Set<JoinNode> collectionJoins = joinManager.buildClause(sbSelectFrom, ID_QUERY_CLAUSE_EXCLUSIONS, PAGE_POSITION_ID_QUERY_ALIAS_PREFIX, true, false, true, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, idNodesToFetch, identifierExpressionsToUseNonRootJoinNodes, null);
        whereManager.buildClause(sbSelectFrom, whereClauseConjuncts, optionalWhereClauseConjuncts);

        boolean inverseOrder = false;

        if (hasGroupBy || havingManager.hasPredicates() || !collectionJoins.isEmpty()) {
            groupByManager.buildGroupBy(sbSelectFrom, ID_QUERY_GROUP_BY_CLAUSE_EXCLUSIONS, getIdentifierExpressionsToUse());
            havingManager.buildClause(sbSelectFrom);
        }

        // Resolve select aliases because we might omit the select items
        orderByManager.buildOrderBy(sbSelectFrom, inverseOrder, true, false, false);

        queryGenerator.setAliasPrefix(null);
        return sbSelectFrom.toString();
    }

    private String buildPageIdQueryString(boolean externalRepresentation) {
        StringBuilder sbSelectFrom = new StringBuilder();
        if (externalRepresentation && isMainQuery) {
            mainQuery.cteManager.buildClause(sbSelectFrom);
        }
        buildPageIdQueryString(sbSelectFrom, false, externalRepresentation);
        return sbSelectFrom.toString();
    }

    private String buildPageIdQueryString(StringBuilder sbSelectFrom, boolean aliasFunction, boolean externalRepresentation) {
        sbSelectFrom.append("SELECT ");
        queryGenerator.setQueryBuffer(sbSelectFrom);
        queryGenerator.setClauseType(ClauseType.SELECT);
        ResolvedExpression[] identifierExpressionsToUse = getIdentifierExpressionsToUse();

        if (aliasFunction && !externalRepresentation && needsNewIdList) {
            for (int i = 0; i < identifierExpressionsToUse.length; i++) {
                sbSelectFrom.append(mainQuery.jpaProvider.getCustomFunctionInvocation(AliasFunction.FUNCTION_NAME, 1));
                identifierExpressionsToUse[i].getExpression().accept(queryGenerator);
                sbSelectFrom.append(",'").append(ColumnTruncFunction.SYNTHETIC_COLUMN_PREFIX);
                sbSelectFrom.append(i);
                sbSelectFrom.append("')");
                if (identifierToUseSelectAliases[i] != null) {
                    sbSelectFrom.append(" AS ");
                    sbSelectFrom.append(selectManager.getSubquerySelectAlias(identifierToUseSelectAliases[i]));
                }
                sbSelectFrom.append(", ");
            }
        } else {
            for (int i = 0; i < identifierExpressionsToUse.length; i++) {
                identifierExpressionsToUse[i].getExpression().accept(queryGenerator);
                if (identifierToUseSelectAliases[i] != null) {
                    sbSelectFrom.append(" AS ");
                    sbSelectFrom.append(identifierToUseSelectAliases[i]);
                }
                sbSelectFrom.append(", ");
            }
        }

        sbSelectFrom.setLength(sbSelectFrom.length() - 2);

        if (needsNewIdList) {
            if (isWithInlineIdQuery()) {
                // We need to pass a null keysetToSelectIndexMapping in this case to force rendering the order by alias expressions to the id query
                orderByManager.buildSelectClauses(sbSelectFrom, false, aliasFunction && !externalRepresentation, null);
            } else {
                orderByManager.buildSelectClauses(sbSelectFrom, keysetExtraction, aliasFunction && !externalRepresentation, keysetToSelectIndexMapping);
            }
        }

        if (!aliasFunction && firstResult < maximumCount && withCountQuery && withInlineCountQuery) {
            sbSelectFrom.append(", ");
            appendPageCountQueryAsSubquery(sbSelectFrom, externalRepresentation);
        }

        List<String> whereClauseConjuncts = new ArrayList<>();
        List<String> optionalWhereClauseConjuncts = new ArrayList<>();
        // The id query does not have any fetch owners
        // Note that we always exclude the nodes with group by dependency. We consider just the ones from the identifiers
        Set<JoinNode> idNodesToFetch = Collections.emptySet();
        Set<JoinNode> identifierExpressionsToUseNonRootJoinNodes = getIdentifierExpressionsToUseNonRootJoinNodes();
        Set<JoinNode> collectionJoins = joinManager.buildClause(sbSelectFrom, ID_QUERY_GROUP_BY_CLAUSE_EXCLUSIONS, null, true, externalRepresentation, true, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, idNodesToFetch, identifierExpressionsToUseNonRootJoinNodes, null);

        if (keysetMode == KeysetMode.NONE || keysetManager.getKeysetLink().getKeyset().getTuple() == null) {
            whereManager.buildClause(sbSelectFrom, whereClauseConjuncts, optionalWhereClauseConjuncts);
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
                whereManager.buildClausePredicate(sbSelectFrom, whereClauseConjuncts, optionalWhereClauseConjuncts);
            }
        }

        boolean inverseOrder = keysetMode == KeysetMode.PREVIOUS;

        // We could avoid rendering the group by clause if the collection joins aren't referenced
        // We could also build a minimal group by clause when hasGroupBy == false, otherwise we require the clause
        if (hasGroupBy || havingManager.hasPredicates() || !collectionJoins.isEmpty()) {
            groupByManager.buildGroupBy(sbSelectFrom, ID_QUERY_GROUP_BY_CLAUSE_EXCLUSIONS, getIdentifierExpressionsToUse());
            havingManager.buildClause(sbSelectFrom);
        }

        // Resolve select aliases to their actual expressions only if the select items aren't included
        orderByManager.buildOrderBy(sbSelectFrom, inverseOrder, !needsNewIdList, needsNewIdList, aliasFunction && !externalRepresentation);

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
        buildBaseQueryString(sbSelectFrom, externalRepresentation, null);
        return sbSelectFrom.toString();
    }

    @Override
    protected void buildBaseQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation, JoinNode lateralJoinNode) {
        selectManager.buildSelect(sbSelectFrom, false, externalRepresentation);

        /**
         * we have already selected the IDs so now we only need to select the
         * fields and apply the ordering all other clauses are not required any
         * more and therefore we can also omit any joins which the SELECT or the
         * ORDER_BY clause do not depend on
         */
        List<String> whereClauseConjuncts = new ArrayList<>();
        // We always have a where clause, so no need for an separate collection
        List<String> optionalWhereClauseConjuncts = whereClauseConjuncts;
        joinManager.buildClause(sbSelectFrom, OBJECT_QUERY_CLAUSE_EXCLUSIONS, null, false, externalRepresentation, false, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, nodesToFetch, Collections.<JoinNode>emptySet(), null);
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

        orderByManager.buildOrderBy(sbSelectFrom, false, false, false, false);
    }

    private String buildObjectQueryString(boolean externalRepresentation) {
        StringBuilder sbSelectFrom = new StringBuilder();
        if (externalRepresentation && isMainQuery) {
            mainQuery.cteManager.buildClause(sbSelectFrom);
        }
        buildObjectQueryString(sbSelectFrom, externalRepresentation);
        return sbSelectFrom.toString();
    }

    private void appendPageIdQueryAsSubquery(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        appendQueryAsSubquery(false, sbSelectFrom, externalRepresentation);
    }

    private void appendPageCountQueryAsSubquery(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        appendQueryAsSubquery(true, sbSelectFrom, externalRepresentation);
    }

    private void appendQueryAsSubquery(boolean count, StringBuilder sbSelectFrom, boolean externalRepresentation) {
        Set<JoinNode> keyRestrictedLeftJoins = getKeyRestrictedLeftJoins();
        Set<JoinNode> alwaysIncludedNodes = getIdentifierExpressionsToUseNonRootJoinNodes();
        Set<ClauseType> clauseExclusions = count ? COUNT_QUERY_GROUP_BY_CLAUSE_EXCLUSIONS : ID_QUERY_GROUP_BY_CLAUSE_EXCLUSIONS;
        List<JoinNode> entityFunctions = null;
        boolean normalQueryMode = !isMainQuery || (!mainQuery.cteManager.hasCtes() && (entityFunctions = joinManager.getEntityFunctions(clauseExclusions, true, alwaysIncludedNodes)).isEmpty() && keyRestrictedLeftJoins.isEmpty());
        JoinNode dualNode = null;
        if (count && maximumCount != Long.MAX_VALUE) {
            dualNode = createDualNode();
        }
        if (externalRepresentation || normalQueryMode) {
            List<EntityFunctionNode> entityFunctionNodes = null;
            if (dualNode != null) {
                entityFunctionNodes = getEntityFunctionNodes(null, Collections.singletonList(dualNode), Collections.<JoinNode>emptyList(), false);
                for (int i = 0; i < entityFunctionNodes.size(); i++) {
                    sbSelectFrom.append(mainQuery.jpaProvider.getCustomFunctionInvocation(EntityFunction.FUNCTION_NAME, 1));
                }
            }
            sbSelectFrom.append("(");
            if (count) {
                buildPageCountQueryString(sbSelectFrom, externalRepresentation, false, maximumCount);
            } else {
                buildPageIdQueryString(sbSelectFrom, true, externalRepresentation);
            }
            sbSelectFrom.append(')');
            if (dualNode != null) {
                finishEntityFunctionNodes(sbSelectFrom, entityFunctionNodes);
            }
        } else {
            if (dualNode != null) {
                entityFunctions = new ArrayList<>();
                entityFunctions.add(dualNode);
            }
            if (entityFunctions == null) {
                entityFunctions = joinManager.getEntityFunctions(clauseExclusions, true, alwaysIncludedNodes);
            }
            if (entityFunctions.isEmpty()) {
                sbSelectFrom.append("(");
                if (count) {
                    buildPageCountQueryString(sbSelectFrom, externalRepresentation, false, maximumCount);
                } else {
                    buildPageIdQueryString(sbSelectFrom, true, externalRepresentation);
                }
                sbSelectFrom.append(')');
            } else {
                List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(null, entityFunctions);
                for (int i = 0; i < entityFunctionNodes.size(); i++) {
                    sbSelectFrom.append(mainQuery.jpaProvider.getCustomFunctionInvocation(EntityFunction.FUNCTION_NAME, 1));
                }

                sbSelectFrom.append("(");
                if (count) {
                    buildPageCountQueryString(sbSelectFrom, externalRepresentation, false, maximumCount);
                } else {
                    buildPageIdQueryString(sbSelectFrom, true, externalRepresentation);
                }
                sbSelectFrom.append(')');

                finishEntityFunctionNodes(sbSelectFrom, entityFunctionNodes);
            }
        }
    }

    private String buildObjectQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        selectManager.buildSelect(sbSelectFrom, false, externalRepresentation);

        if (keysetExtraction) {
            if (selectManager.getSelectInfos().size() == 0 && isWithInlineIdQuery()) {
                // We need to pass a null keysetToSelectIndexMapping in this case to force rendering the keyset relevant expressions to the object query
                orderByManager.buildSelectClauses(sbSelectFrom, true, false, null);
            } else {
                orderByManager.buildSelectClauses(sbSelectFrom, true, false, keysetToSelectIndexMapping);
            }
        }

        if (firstResult < maximumCount && withCountQuery && withInlineCountQuery) {
            sbSelectFrom.append(", ");
            appendPageCountQueryAsSubquery(sbSelectFrom, externalRepresentation);
        }

        List<String> whereClauseConjuncts = new ArrayList<>();
        List<String> optionalWhereClauseConjuncts = new ArrayList<>();

        if (isWithInlineIdQuery() && (hasCollections || withForceIdQuery)) {
            joinManager.buildClause(sbSelectFrom, OBJECT_QUERY_CLAUSE_EXCLUSIONS, null, false, externalRepresentation, false, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, nodesToFetch, Collections.<JoinNode>emptySet(), null);

            ResolvedExpression[] identifierExpressions = getIdentifierExpressions();
            ResolvedExpression[] resultUniqueExpressions = getUniqueIdentifierExpressions();

            if (resultUniqueExpressions != null) {
                identifierExpressions = resultUniqueExpressions;
            }

            sbSelectFrom.append(" WHERE ");
            queryGenerator.setQueryBuffer(sbSelectFrom);
            if (identifierExpressions.length == 1) {
                identifierExpressions[0].getExpression().accept(queryGenerator);
                sbSelectFrom.append(" IN ");
                sbSelectFrom.append('(');

                if (!externalRepresentation) {
                    if (needsNewIdList) {
                        sbSelectFrom.append(mainQuery.jpaProvider.getCustomFunctionInvocation(ColumnTruncFunction.FUNCTION_NAME, 1));
                    } else if (!mainQuery.dbmsDialect.supportsLimitInQuantifiedPredicateSubquery()) {
                        sbSelectFrom.append(mainQuery.jpaProvider.getCustomFunctionInvocation(QueryWrapperFunction.FUNCTION_NAME, 1));
                    }
                }

                sbSelectFrom.append(mainQuery.jpaProvider.getCustomFunctionInvocation(LimitFunction.FUNCTION_NAME, 1));
                appendPageIdQueryAsSubquery(sbSelectFrom, externalRepresentation);
                sbSelectFrom.append(',').append(maxResults);
                if (firstResult != 0 && (keysetMode == KeysetMode.NONE || keysetManager.getKeysetLink().getKeyset().getTuple() == null)) {
                    sbSelectFrom.append(',').append(firstResult);
                }
                sbSelectFrom.append(')');
                if (!externalRepresentation) {
                    if (needsNewIdList) {
                        sbSelectFrom.append(",").append(identifierExpressions.length).append(')');
                    } else if (!mainQuery.dbmsDialect.supportsLimitInQuantifiedPredicateSubquery()) {
                        sbSelectFrom.append(')');
                    }
                }
                sbSelectFrom.append(')');
            } else {
                sbSelectFrom.append(mainQuery.jpaProvider.getCustomFunctionInvocation(RowValueSubqueryComparisonFunction.FUNCTION_NAME, 1))
                        .append('\'').append("IN").append('\'');

                for (int j = 0; j < identifierExpressions.length; j++) {
                    sbSelectFrom.append(',');
                    identifierExpressions[j].getExpression().accept(queryGenerator);
                }
                sbSelectFrom.append(',');

                if (!externalRepresentation) {
                    if (needsNewIdList) {
                        sbSelectFrom.append(mainQuery.jpaProvider.getCustomFunctionInvocation(ColumnTruncFunction.FUNCTION_NAME, 1));
                    } else if (!mainQuery.dbmsDialect.supportsLimitInQuantifiedPredicateSubquery()) {
                        sbSelectFrom.append(mainQuery.jpaProvider.getCustomFunctionInvocation(QueryWrapperFunction.FUNCTION_NAME, 1));
                    }
                }

                sbSelectFrom.append(mainQuery.jpaProvider.getCustomFunctionInvocation(LimitFunction.FUNCTION_NAME, 1));
                appendPageIdQueryAsSubquery(sbSelectFrom, externalRepresentation);
                sbSelectFrom.append(',').append(maxResults);
                if (firstResult != 0 && (keysetMode == KeysetMode.NONE || keysetManager.getKeysetLink().getKeyset().getTuple() == null)) {
                    sbSelectFrom.append(',').append(firstResult);
                }
                sbSelectFrom.append(')');
                if (!externalRepresentation) {
                    if (needsNewIdList) {
                        sbSelectFrom.append(",").append(identifierExpressions.length).append(')');
                    } else if (!mainQuery.dbmsDialect.supportsLimitInQuantifiedPredicateSubquery()) {
                        sbSelectFrom.append(')');
                    }
                }
                sbSelectFrom.append(") = 0");
            }

            if (!whereClauseConjuncts.isEmpty()) {
                sbSelectFrom.append(" AND ");
                whereManager.buildClausePredicate(sbSelectFrom, whereClauseConjuncts, optionalWhereClauseConjuncts);
            }

            if (hasGroupBy) {
                groupByManager.buildGroupBy(sbSelectFrom, OBJECT_QUERY_CLAUSE_EXCLUSIONS);
                havingManager.buildClause(sbSelectFrom);
            }
        } else {
            joinManager.buildClause(sbSelectFrom, hasGroupBy ? NO_CLAUSE_EXCLUSION : OBJECT_QUERY_WITHOUT_GROUP_BY_EXCLUSIONS, null, false, externalRepresentation, false, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, nodesToFetch, Collections.<JoinNode>emptySet(), null);

            if (keysetMode == KeysetMode.NONE || keysetManager.getKeysetLink().getKeyset().getTuple() == null) {
                whereManager.buildClause(sbSelectFrom, whereClauseConjuncts, optionalWhereClauseConjuncts);
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
                    whereManager.buildClausePredicate(sbSelectFrom, whereClauseConjuncts, optionalWhereClauseConjuncts);
                }
            }
            appendGroupByClause(sbSelectFrom);
        }

        boolean inverseOrder = keysetMode == KeysetMode.PREVIOUS;

        orderByManager.buildOrderBy(sbSelectFrom, inverseOrder, false, false, false);

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
