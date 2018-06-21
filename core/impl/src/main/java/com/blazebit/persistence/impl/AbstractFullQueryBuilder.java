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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import com.blazebit.persistence.impl.function.count.AbstractCountFunction;
import com.blazebit.persistence.impl.query.CTENode;
import com.blazebit.persistence.impl.query.CustomQuerySpecification;
import com.blazebit.persistence.impl.query.CustomSQLTypedQuery;
import com.blazebit.persistence.impl.query.EntityFunctionNode;
import com.blazebit.persistence.impl.query.QuerySpecification;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;

/**
 *
 * @param <T> The query result type
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public abstract class AbstractFullQueryBuilder<T, X extends FullQueryBuilder<T, X>, Z, W, FinalSetReturn extends BaseFinalSetOperationBuilderImpl<T, ?, ?>> extends AbstractQueryBuilder<T, X, Z, W, FinalSetReturn> implements FullQueryBuilder<T, X> {

    /**
     * This flag indicates whether the current builder has been used to create a
     * PaginatedCriteriaBuilder. In this case we must not allow any calls to
     * group by and distinct since the corresponding managers are shared with
     * the PaginatedCriteriaBuilder and any changes would affect the
     * PaginatedCriteriaBuilder as well.
     */
    private boolean createdPaginatedBuilder = false;

    private String cachedCountQueryString;

    /**
     * Create flat copy of builder
     *
     * @param builder
     */
    protected AbstractFullQueryBuilder(AbstractFullQueryBuilder<T, ? extends FullQueryBuilder<T, ?>, ?, ?, ?> builder) {
        super(builder);
    }

    public AbstractFullQueryBuilder(MainQuery mainQuery, boolean isMainQuery, Class<T> clazz, String alias, FinalSetReturn finalSetOperationBuilder) {
        super(mainQuery, isMainQuery, clazz, alias, finalSetOperationBuilder);
    }

    @Override
    protected void prepareForModification() {
        super.prepareForModification();
        cachedCountQueryString = null;
    }

    @Override
    public <Y> FullQueryBuilder<Y, ?> copy(Class<Y> resultClass) {
        prepareAndCheck();
        MainQuery mainQuery = cbf.createMainQuery(getEntityManager());
        CriteriaBuilderImpl<Y> newBuilder = new CriteriaBuilderImpl<Y>(mainQuery, true, resultClass, null);
        newBuilder.fromClassExplicitlySet = true;

        newBuilder.parameterManager.applyFrom(parameterManager);
        mainQuery.cteManager.applyFrom(this.mainQuery.cteManager);
        newBuilder.aliasManager.applyFrom(aliasManager);
        newBuilder.joinManager.applyFrom(joinManager);
        newBuilder.whereManager.applyFrom(whereManager);
        newBuilder.havingManager.applyFrom(havingManager);
        newBuilder.groupByManager.applyFrom(groupByManager);
        newBuilder.orderByManager.applyFrom(orderByManager);

        newBuilder.setFirstResult(firstResult);
        newBuilder.setMaxResults(maxResults);

        // TODO: set operations?
        // TODO: select aliases that are ordered by?

        newBuilder.selectManager.setDefaultSelect(selectManager.getSelectInfos());

        return newBuilder;
    }

    private String getCountQueryStringWithoutCheck() {
        if (cachedCountQueryString == null) {
            cachedCountQueryString = buildPageCountQueryString(false);
        }

        return cachedCountQueryString;
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
        JoinNode rootNode = joinManager.getRootNodeOrFail("Count queries do not support multiple from clause elements!");
        Attribute<?, ?> idAttribute = JpaMetamodelUtils.getIdAttribute(mainQuery.metamodel.entity(rootNode.getJavaType()));
        String idName = idAttribute.getName();
        StringBuilder idClause = new StringBuilder(100);
        rootNode.appendDeReference(idClause, idName);
        // Spaces are important to be able to reuse the string builder without copying
        String countString = jpaProvider.getCustomFunctionInvocation(AbstractCountFunction.FUNCTION_NAME, 1) + "'DISTINCT'," + idClause + ")";
        sbSelectFrom.append("SELECT ").append(countString);

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

    @Override
    public TypedQuery<Long> getCountQuery() {
        prepareAndCheck();
        // We can only use the query directly if we have no ctes, entity functions or hibernate bugs
        Set<JoinNode> keyRestrictedLeftJoins = joinManager.getKeyRestrictedLeftJoins();
        boolean normalQueryMode = !isMainQuery || (!mainQuery.cteManager.hasCtes() && !joinManager.hasEntityFunctions() && keyRestrictedLeftJoins.isEmpty());
        String countQueryString = getCountQueryStringWithoutCheck();

        if (normalQueryMode && isEmpty(keyRestrictedLeftJoins, EnumSet.of(ClauseType.ORDER_BY, ClauseType.SELECT))) {
            TypedQuery<Long> countQuery = em.createQuery(countQueryString, Long.class);
            if (isCacheable()) {
                jpaProvider.setCacheable(countQuery);
            }
            parameterManager.parameterizeQuery(countQuery);
            return countQuery;
        }

        TypedQuery<Long> baseQuery = em.createQuery(countQueryString, Long.class);
        Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery);
        List<String> keyRestrictedLeftJoinAliases = getKeyRestrictedLeftJoinAliases(baseQuery, keyRestrictedLeftJoins, EnumSet.of(ClauseType.ORDER_BY, ClauseType.SELECT));
        List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(baseQuery);
        boolean shouldRenderCteNodes = renderCteNodes(false);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(baseQuery, false) : Collections.EMPTY_LIST;
        QuerySpecification querySpecification = new CustomQuerySpecification(
                this, baseQuery, parameterManager.getParameters(), parameterListNames, null, null, keyRestrictedLeftJoinAliases, entityFunctionNodes, mainQuery.cteManager.isRecursive(), ctes, shouldRenderCteNodes
        );

        TypedQuery<Long> countQuery = new CustomSQLTypedQuery<>(
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
    public PaginatedCriteriaBuilder<T> page(int firstRow, int pageSize) {
        prepareForModification();
        if (selectManager.isDistinct()) {
            throw new IllegalStateException("Cannot paginate a DISTINCT query");
        }
        if (!groupByManager.isEmpty()) {
            throw new IllegalStateException("Cannot paginate a GROUP BY query");
        }
        createdPaginatedBuilder = true;
        return new PaginatedCriteriaBuilderImpl<T>(this, false, null, firstRow, pageSize);
    }

    @Override
    public PaginatedCriteriaBuilder<T> page(Object entityId, int pageSize) {
        prepareForModification();
        if (selectManager.isDistinct()) {
            throw new IllegalStateException("Cannot paginate a DISTINCT query");
        }
        if (!groupByManager.isEmpty()) {
            throw new IllegalStateException("Cannot paginate a GROUP BY query");
        }
        checkEntityId(entityId);
        createdPaginatedBuilder = true;
        return new PaginatedCriteriaBuilderImpl<T>(this, false, entityId, pageSize);
    }

    @Override
    public PaginatedCriteriaBuilder<T> page(KeysetPage keysetPage, int firstRow, int pageSize) {
        prepareForModification();
        if (selectManager.isDistinct()) {
            throw new IllegalStateException("Cannot paginate a DISTINCT query");
        }
        if (!groupByManager.isEmpty()) {
            throw new IllegalStateException("Cannot paginate a GROUP BY query");
        }
        createdPaginatedBuilder = true;
        return new PaginatedCriteriaBuilderImpl<T>(this, true, keysetPage, firstRow, pageSize);
    }

    private void checkEntityId(Object entityId) {
        if (entityId == null) {
            throw new IllegalArgumentException("Invalid null entity id given");
        }

        EntityType<?> entityType = mainQuery.metamodel.entity(joinManager.getRootNodeOrFail("Paginated queries do not support multiple from clause elements!").getJavaType());
        Attribute<?, ?> idAttribute = JpaMetamodelUtils.getIdAttribute(entityType);
        Class<?> idType = JpaMetamodelUtils.resolveFieldClass(entityType.getJavaType(), idAttribute);

        if (!idType.isInstance(entityId)) {
            throw new IllegalArgumentException("The type of the given entity id '" + entityId.getClass().getName()
                + "' is not an instance of the expected id type '" + idType.getName() + "' of the entity class '" + entityType.getJavaType().getName() + "'");
        }
    }

    @Override
    public <Y> SelectObjectBuilder<? extends FullQueryBuilder<Y, ?>> selectNew(Class<Y> clazz) {
        prepareForModification();
        if (clazz == null) {
            throw new NullPointerException("clazz");
        }

        verifyBuilderEnded();
        return selectManager.selectNew(this, clazz);
    }

    @Override
    public <Y> SelectObjectBuilder<? extends FullQueryBuilder<Y, ?>> selectNew(Constructor<Y> constructor) {
        prepareForModification();
        if (constructor == null) {
            throw new NullPointerException("constructor");
        }

        verifyBuilderEnded();
        return selectManager.selectNew(this, constructor);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y> FullQueryBuilder<Y, ?> selectNew(ObjectBuilder<Y> objectBuilder) {
        prepareForModification();
        if (objectBuilder == null) {
            throw new NullPointerException("objectBuilder");
        }

        verifyBuilderEnded();
        selectManager.selectNew((X) this, objectBuilder);
        return (FullQueryBuilder<Y, ?>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public X fetch(String path) {
        prepareForModification();
        verifyBuilderEnded();
        joinManager.implicitJoin(expressionFactory.createPathExpression(path), true, null, null, null, false, false, true, false, true);
        return (X) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public X fetch(String... paths) {
        prepareForModification();
        verifyBuilderEnded();

        for (String path : paths) {
            joinManager.implicitJoin(expressionFactory.createPathExpression(path), true, null, null, null, false, false, true, false, true);
        }

        return (X) this;
    }

    @Override
    public X innerJoinFetch(String path, String alias) {
        return join(path, alias, JoinType.INNER, true);
    }

    @Override
    public X innerJoinFetchDefault(String path, String alias) {
        return joinDefault(path, alias, JoinType.INNER, true);
    }

    @Override
    public X leftJoinFetch(String path, String alias) {
        return join(path, alias, JoinType.LEFT, true);
    }

    @Override
    public X leftJoinFetchDefault(String path, String alias) {
        return joinDefault(path, alias, JoinType.LEFT, true);
    }

    @Override
    public X rightJoinFetch(String path, String alias) {
        return join(path, alias, JoinType.RIGHT, true);
    }

    @Override
    public X rightJoinFetchDefault(String path, String alias) {
        return joinDefault(path, alias, JoinType.RIGHT, true);
    }

    @Override
    public X join(String path, String alias, JoinType type, boolean fetch) {
        return join(path, alias, type, fetch, false);
    }

    @Override
    public X joinDefault(String path, String alias, JoinType type, boolean fetch) {
        return join(path, alias, type, fetch, true);
    }

    @SuppressWarnings("unchecked")
    private X join(String path, String alias, JoinType type, boolean fetch, boolean defaultJoin) {
        prepareForModification();
        if (path == null) {
            throw new NullPointerException("path");
        }
        if (alias == null) {
            throw new NullPointerException("alias");
        }
        if (type == null) {
            throw new NullPointerException("type");
        }
        if (alias.isEmpty()) {
            throw new IllegalArgumentException("Empty alias");
        }

        verifyBuilderEnded();
        joinManager.join(path, alias, type, fetch, defaultJoin);
        return (X) this;
    }

    @Override
    public X groupBy(String expression) {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling groupBy() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.groupBy(expression);
    }

    @Override
    public X groupBy(String... paths) {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling groupBy() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.groupBy(paths);
    }

    @Override
    public X distinct() {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling distinct() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.distinct();
    }

}
