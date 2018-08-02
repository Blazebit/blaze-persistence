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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;

import com.blazebit.persistence.CaseWhenStarterBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.HavingOrBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.function.count.AbstractCountFunction;
import com.blazebit.persistence.impl.query.CTENode;
import com.blazebit.persistence.impl.query.CustomQuerySpecification;
import com.blazebit.persistence.impl.query.CustomSQLTypedQuery;
import com.blazebit.persistence.impl.query.EntityFunctionNode;
import com.blazebit.persistence.impl.query.QuerySpecification;
import com.blazebit.persistence.parser.expression.Expression;
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

    protected static final Set<ClauseType> NO_CLAUSE_EXCLUSION = EnumSet.noneOf(ClauseType.class);
    protected static final Set<ClauseType> COUNT_QUERY_CLAUSE_EXCLUSIONS = EnumSet.of(ClauseType.ORDER_BY, ClauseType.SELECT);

    protected String cachedCountQueryString;
    protected String cachedExternalCountQueryString;
    protected ResolvedExpression[] cachedGroupByIdentifierExpressions;

    /**
     * This flag indicates whether the current builder has been used to create a
     * PaginatedCriteriaBuilder. In this case we must not allow any calls to
     * group by and distinct since the corresponding managers are shared with
     * the PaginatedCriteriaBuilder and any changes would affect the
     * PaginatedCriteriaBuilder as well.
     */
    private boolean createdPaginatedBuilder = false;
    private boolean explicitPaginatedIdentifier = false;

    private ResolvedExpression[] entityIdentifierExpressions;
    private ResolvedExpression[] uniqueIdentifierExpressions;

    /**
     * Create flat copy of builder
     *
     * @param builder
     */
    protected AbstractFullQueryBuilder(AbstractFullQueryBuilder<T, ? extends FullQueryBuilder<T, ?>, ?, ?, ?> builder) {
        super(builder);
        this.entityIdentifierExpressions = builder.entityIdentifierExpressions;
    }

    public AbstractFullQueryBuilder(MainQuery mainQuery, boolean isMainQuery, Class<T> clazz, String alias, FinalSetReturn finalSetOperationBuilder) {
        super(mainQuery, isMainQuery, clazz, alias, finalSetOperationBuilder);
    }

    @Override
    protected void prepareForModification() {
        super.prepareForModification();
        cachedCountQueryString = null;
        cachedExternalCountQueryString = null;
        cachedGroupByIdentifierExpressions = null;
        uniqueIdentifierExpressions = null;
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
            cachedCountQueryString = buildPageCountQueryString(false, true);
        }

        return cachedCountQueryString;
    }

    private String getExternalCountQueryString() {
        if (cachedExternalCountQueryString == null) {
            cachedExternalCountQueryString = buildPageCountQueryString(true, true);
        }

        return cachedExternalCountQueryString;
    }

    protected String buildPageCountQueryString(boolean externalRepresentation, boolean countAll) {
        StringBuilder sbSelectFrom = new StringBuilder();
        if (externalRepresentation && isMainQuery) {
            mainQuery.cteManager.buildClause(sbSelectFrom);
        }
        return buildPageCountQueryString(sbSelectFrom, externalRepresentation, countAll && !hasGroupBy);
    }

    protected final String buildPageCountQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation, boolean countAll) {
        sbSelectFrom.append("SELECT ");
        int countStartIdx = sbSelectFrom.length();
        int countEndIdx;
        boolean isResultUnique;
        if (countAll) {
            if (jpaProvider.supportsCountStar()) {
                sbSelectFrom.append("COUNT(*)");
            } else {
                sbSelectFrom.append(jpaProvider.getCustomFunctionInvocation("count_star", 0)).append(')');
            }
            countEndIdx = sbSelectFrom.length() - 1;
            isResultUnique = true;
        } else {
            sbSelectFrom.append(jpaProvider.getCustomFunctionInvocation(AbstractCountFunction.FUNCTION_NAME, 1));
            sbSelectFrom.append("'DISTINCT',");

            isResultUnique = appendIdentifierExpressions(sbSelectFrom);

            sbSelectFrom.append(")");
            countEndIdx = sbSelectFrom.length() - 1;

            appendPageCountQueryStringExtensions(sbSelectFrom);
        }

        List<String> whereClauseConjuncts = new ArrayList<>();
        // The count query does not have any fetch owners
        Set<JoinNode> countNodesToFetch = Collections.emptySet();

        if (countAll) {
            joinManager.buildClause(sbSelectFrom, NO_CLAUSE_EXCLUSION, null, false, externalRepresentation, whereClauseConjuncts, null, explicitVersionEntities, countNodesToFetch);
            whereManager.buildClause(sbSelectFrom, whereClauseConjuncts, null);
        } else {
            // Collect usage of collection join nodes to optimize away the count distinct
            Set<JoinNode> collectionJoinNodes = joinManager.buildClause(sbSelectFrom, COUNT_QUERY_CLAUSE_EXCLUSIONS, null, true, externalRepresentation, whereClauseConjuncts, null, explicitVersionEntities, countNodesToFetch);
            // TODO: Maybe we can improve this and treat array access joins like non-collection join nodes
            boolean hasCollectionJoinUsages = collectionJoinNodes.size() > 0;

            whereManager.buildClause(sbSelectFrom, whereClauseConjuncts, null);

            // Instead of a count distinct, we render a count(*) if we have no collection joins and the identifier expression is result unique
            // It is result unique when it contains the query root primary key or a unique key that of a uniqueness preserving association of that
            if (!hasCollectionJoinUsages && isResultUnique) {
                String countStar;
                if (jpaProvider.supportsCountStar()) {
                    countStar = "COUNT(*";
                } else {
                    countStar = jpaProvider.getCustomFunctionInvocation("count_star", 0);
                }
                for (int i = countStartIdx, j = 0; i < countEndIdx; i++, j++) {
                    if (j < countStar.length()) {
                        sbSelectFrom.setCharAt(i, countStar.charAt(j));
                    } else {
                        sbSelectFrom.setCharAt(i, ' ');
                    }
                }
            }
        }

        return sbSelectFrom.toString();
    }

    protected void appendPageCountQueryStringExtensions(StringBuilder sbSelectFrom) {
    }

    protected boolean appendIdentifierExpressions(StringBuilder sbSelectFrom) {
        boolean isResultUnique;
        ResolvedExpression[] identifierExpressions = getIdentifierExpressions();
        ResolvedExpression[] resultUniqueExpressions = getUniqueIdentifierExpressions();

        if (resultUniqueExpressions == null) {
            isResultUnique = false;
        } else {
            // We only render the identifiers that are necessary to make it unique
            identifierExpressions = resultUniqueExpressions;
            isResultUnique = true;
        }

        queryGenerator.setQueryBuffer(sbSelectFrom);
        for (int i = 0; i < identifierExpressions.length; i++) {
            identifierExpressions[i].getExpression().accept(queryGenerator);
            sbSelectFrom.append(", ");
        }
        sbSelectFrom.setLength(sbSelectFrom.length() - 2);
        return isResultUnique;
    }

    protected ResolvedExpression[] getUniqueIdentifierExpressions() {
        if (uniqueIdentifierExpressions == null) {
            // If we have a group by clause, we must use that for the count query
            if (hasGroupBy) {
                uniqueIdentifierExpressions = groupByManager.buildMinimalClause();
            } else {
                ResolvedExpression[] identifierExpressions = getIdentifierExpressions();
                // Fast path, if we see that the identifier expressions are the entity identifier expressions, we don't need to check uniqueness
                if (identifierExpressions == entityIdentifierExpressions) {
                    uniqueIdentifierExpressions = identifierExpressions;
                } else {
                    uniqueIdentifierExpressions = uniquenessDetectionVisitor.getResultUniqueExpressions(identifierExpressions);
                }
            }
        }

        return uniqueIdentifierExpressions;
    }

    protected ResolvedExpression[] getIdentifierExpressionsToUse() {
        ResolvedExpression[] identifierExpressions = getIdentifierExpressions();
        ResolvedExpression[] resultUniqueExpressions = getUniqueIdentifierExpressions();

        if (resultUniqueExpressions == null) {
            return identifierExpressions;
        }
        return resultUniqueExpressions;
    }

    protected ResolvedExpression[] getIdentifierExpressions() {
        if (hasGroupBy) {
            if (cachedGroupByIdentifierExpressions == null) {
                Set<ResolvedExpression> resolvedExpressions = groupByManager.getCollectedGroupByClauses().keySet();
                cachedGroupByIdentifierExpressions = resolvedExpressions.toArray(new ResolvedExpression[resolvedExpressions.size()]);
            }
            return cachedGroupByIdentifierExpressions;
        }

        return getQueryRootEntityIdentifierExpressions();
    }

    @Override
    public String getCountQueryString() {
        if (!havingManager.isEmpty()) {
            throw new IllegalStateException("Cannot count a HAVING query yet!");
        }
        prepareAndCheck();
        return getExternalCountQueryString();
    }

    @Override
    public TypedQuery<Long> getCountQuery() {
        if (!havingManager.isEmpty()) {
            throw new IllegalStateException("Cannot count a HAVING query yet!");
        }
        return getCountQuery(getCountQueryStringWithoutCheck());
    }

    protected TypedQuery<Long> getCountQuery(String countQueryString) {
        prepareAndCheck();
        // We can only use the query directly if we have no ctes, entity functions or hibernate bugs
        Set<JoinNode> keyRestrictedLeftJoins = joinManager.getKeyRestrictedLeftJoins();
        boolean normalQueryMode = !isMainQuery || (!mainQuery.cteManager.hasCtes() && !joinManager.hasEntityFunctions() && keyRestrictedLeftJoins.isEmpty());

        if (normalQueryMode && isEmpty(keyRestrictedLeftJoins, COUNT_QUERY_CLAUSE_EXCLUSIONS)) {
            TypedQuery<Long> countQuery = em.createQuery(countQueryString, Long.class);
            if (isCacheable()) {
                jpaProvider.setCacheable(countQuery);
            }
            parameterManager.parameterizeQuery(countQuery);
            return countQuery;
        }

        TypedQuery<Long> baseQuery = em.createQuery(countQueryString, Long.class);
        Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery);
        List<String> keyRestrictedLeftJoinAliases = getKeyRestrictedLeftJoinAliases(baseQuery, keyRestrictedLeftJoins, COUNT_QUERY_CLAUSE_EXCLUSIONS);
        List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(baseQuery);
        boolean shouldRenderCteNodes = renderCteNodes(false);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(false) : Collections.EMPTY_LIST;
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
        return page(firstRow, pageSize, (ResolvedExpression[]) null);
    }

    @Override
    public PaginatedCriteriaBuilder<T> page(Object entityId, int pageSize) {
        return page(entityId, pageSize, getQueryRootEntityIdentifierExpressions());
    }

    @Override
    public PaginatedCriteriaBuilder<T> page(KeysetPage keysetPage, int firstRow, int pageSize) {
        return page(keysetPage, firstRow, pageSize, (ResolvedExpression[]) null);
    }

    @Override
    public PaginatedCriteriaBuilder<T> page(int firstRow, int pageSize, String identifierExpression) {
        return page(firstRow, pageSize, getIdentifierExpressions(identifierExpression, null));
    }

    @Override
    public PaginatedCriteriaBuilder<T> page(Object entityId, int pageSize, String identifierExpression) {
        return page(entityId, pageSize, getIdentifierExpressions(identifierExpression, null));
    }

    @Override
    public PaginatedCriteriaBuilder<T> page(KeysetPage keysetPage, int firstRow, int pageSize, String identifierExpression) {
        return page(keysetPage, firstRow, pageSize, getIdentifierExpressions(identifierExpression, null));
    }

    @Override
    public PaginatedCriteriaBuilder<T> page(int firstRow, int pageSize, String identifierExpression, String... identifierExpressions) {
        return page(firstRow, pageSize, getIdentifierExpressions(identifierExpression, identifierExpressions));
    }

    @Override
    public PaginatedCriteriaBuilder<T> page(Object entityId, int pageSize, String identifierExpression, String... identifierExpressions) {
        return page(entityId, pageSize, getIdentifierExpressions(identifierExpression, identifierExpressions));
    }

    @Override
    public PaginatedCriteriaBuilder<T> page(KeysetPage keysetPage, int firstRow, int pageSize, String identifierExpression, String... identifierExpressions) {
        return page(keysetPage, firstRow, pageSize, getIdentifierExpressions(identifierExpression, identifierExpressions));
    }

    protected ResolvedExpression[] getQueryRootEntityIdentifierExpressions() {
        if (entityIdentifierExpressions == null) {
            JoinNode rootNode = joinManager.getRootNodeOrFail("Paginated criteria builders do not support multiple from clause elements!");
            Set<SingularAttribute<?, ?>> idAttributes = JpaMetamodelUtils.getIdAttributes(mainQuery.metamodel.entity(rootNode.getJavaType()));
            @SuppressWarnings("unchecked")
            ResolvedExpression[] identifierExpressions = new ResolvedExpression[idAttributes.size()];
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (Attribute<?, ?> idAttribute : idAttributes) {
                String idName = idAttribute.getName();
                sb.setLength(0);
                rootNode.appendDeReference(sb, idName);
                Expression expression = rootNode.createExpression(idName);
                joinManager.implicitJoin(expression, false, null, null, null, false, false, false, false);
                identifierExpressions[i++] = new ResolvedExpression(sb.toString(), expression);
            }
            entityIdentifierExpressions = identifierExpressions;
        }
        return entityIdentifierExpressions;
    }

    private ResolvedExpression[] getIdentifierExpressions(String identifierExpression, String[] identifierExpressions) {
        if (identifierExpression == null) {
            throw new IllegalArgumentException("Invalid null identifier expression passed to page()!");
        }

        List<ResolvedExpression> resolvedExpressions = new ArrayList<>(identifierExpressions == null ? 1 : identifierExpression.length() + 1);
        Expression expression = expressionFactory.createSimpleExpression(identifierExpression, false);
        joinManager.implicitJoin(expression, false, null, null, null, false, false, false, false);
        resolvedExpressions.add(new ResolvedExpression(expression.clone(true).toString(), expression));

        uniquenessDetectionVisitor.clear();
        uniquenessDetectionVisitor.isUnique(expression);

        if (identifierExpressions != null) {
            for (String expressionString : identifierExpressions) {
                expression = expressionFactory.createSimpleExpression(expressionString, false);
                joinManager.implicitJoin(expression, false, null, null, null, false, false, false, false);
                ResolvedExpression resolvedExpression = new ResolvedExpression(expression.clone(true).toString(), expression);
                if (resolvedExpressions.contains(resolvedExpression)) {
                    throw new IllegalArgumentException("Duplicate identifier expression '" + expressionString + "' in " + Arrays.toString(identifierExpressions) + "!");
                }
                resolvedExpressions.add(resolvedExpression);
                uniquenessDetectionVisitor.isUnique(expression);
            }
        }

        @SuppressWarnings("unchecked")
        ResolvedExpression[] entries = resolvedExpressions.toArray(new ResolvedExpression[resolvedExpressions.size()]);
        if (!uniquenessDetectionVisitor.isResultUnique()) {
            throw new IllegalArgumentException("The identifier expressions [" + expressionString(entries) + "] do not form a unique tuple which is required for pagination!");
        }

        return entries;
    }

    private PaginatedCriteriaBuilder<T> page(int firstRow, int pageSize, ResolvedExpression[] identifierExpressions) {
        prepareForModification();
        if (selectManager.isDistinct()) {
            throw new IllegalStateException("Cannot paginate a DISTINCT query");
        }
        if (groupByManager.hasGroupBys() && identifierExpressions != null) {
            throw new IllegalStateException("Cannot paginate a GROUP BY query by the expressions [" + expressionString(identifierExpressions) + "] as it is implicitly paginated by it's group by clause!");
        }
        if (!havingManager.isEmpty()) {
            throw new IllegalStateException("Cannot paginate a HAVING query");
        }
        createdPaginatedBuilder = true;
        explicitPaginatedIdentifier = identifierExpressions != null;
        return new PaginatedCriteriaBuilderImpl<T>(this, false, null, firstRow, pageSize, identifierExpressions);
    }

    private PaginatedCriteriaBuilder<T> page(Object entityId, int pageSize, ResolvedExpression[] identifierExpressions) {
        prepareForModification();
        if (selectManager.isDistinct()) {
            throw new IllegalStateException("Cannot paginate a DISTINCT query");
        }
        if (groupByManager.hasGroupBys() && identifierExpressions != null) {
            throw new IllegalStateException("Cannot paginate a GROUP BY query by the expressions [" + expressionString(identifierExpressions) + "]");
        }
        if (!havingManager.isEmpty()) {
            throw new IllegalStateException("Cannot paginate a HAVING query");
        }
        checkEntityId(entityId, identifierExpressions);
        createdPaginatedBuilder = true;
        explicitPaginatedIdentifier = identifierExpressions != null;
        return new PaginatedCriteriaBuilderImpl<T>(this, false, entityId, pageSize, identifierExpressions);
    }

    private PaginatedCriteriaBuilder<T> page(KeysetPage keysetPage, int firstRow, int pageSize, ResolvedExpression[] identifierExpressions) {
        prepareForModification();
        if (selectManager.isDistinct()) {
            throw new IllegalStateException("Cannot paginate a DISTINCT query");
        }
        if (groupByManager.hasGroupBys() && identifierExpressions != null) {
            throw new IllegalStateException("Cannot paginate a GROUP BY query by the expressions [" + expressionString(identifierExpressions) + "]");
        }
        if (!havingManager.isEmpty()) {
            throw new IllegalStateException("Cannot paginate a HAVING query");
        }
        createdPaginatedBuilder = true;
        explicitPaginatedIdentifier = identifierExpressions != null;
        return new PaginatedCriteriaBuilderImpl<T>(this, true, keysetPage, firstRow, pageSize, identifierExpressions);
    }

    protected static String expressionString(ResolvedExpression[] identifierExpressions) {
        StringBuilder sb = new StringBuilder();
        for (ResolvedExpression identifierExpression : identifierExpressions) {
            sb.append(identifierExpression.getExpressionString()).append(", ");
        }
        sb.setLength(sb.length() - 2);
        return sb.toString();
    }

    private void checkEntityId(Object entityId, ResolvedExpression[] identifierExpressions) {
        if (entityId == null) {
            throw new IllegalArgumentException("Invalid null entity id given");
        }
        if (identifierExpressions.length == 0) {
            throw new IllegalArgumentException("Empty identifier expressions given");
        }
        if (identifierExpressions.length > 1) {
            if (!entityId.getClass().isArray()) {
                throw new IllegalArgumentException("The type of the given entity id '" + entityId.getClass().getName()
                        + "' is not an array of the identifier components " + Arrays.toString(identifierExpressions) + " !");
            }

            Object[] entityIdComponents = (Object[]) entityId;
            if (entityIdComponents.length != identifierExpressions.length) {
                throw new IllegalArgumentException("The number of entity id components is '" + entityIdComponents.length
                        + "' which does not match the number of identifier component expressions " + identifierExpressions.length + " !");
            }

            for (int i = 0; i < identifierExpressions.length; i++) {
                checkEntityIdComponent(entityIdComponents[i], identifierExpressions[i].getExpression(), identifierExpressions[i].getExpressionString());
            }
        } else {
            checkEntityIdComponent(entityId, identifierExpressions[0].getExpression(), "identifier");
        }
    }

    private void checkEntityIdComponent(Object component, Expression expression, String componentName) {
        AttributeHolder attribute = JpaUtils.getAttributeForJoining(getMetamodel(), expression);
        Class<?> type = attribute.getAttributeType().getJavaType();

        if (type == null || !type.isInstance(component)) {
            throw new IllegalArgumentException("The type of the given " + componentName + " '" + component.getClass().getName()
                    + "' is not an instance of the expected type '" + JpaMetamodelUtils.getTypeName(attribute.getAttributeType()) + "'");
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
    public X distinct() {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling distinct() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.distinct();
    }

    @Override
    public RestrictionBuilder<X> having(String expression) {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.having(expression);
    }

    @Override
    public CaseWhenStarterBuilder<RestrictionBuilder<X>> havingCase() {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.havingCase();
    }

    @Override
    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<X>> havingSimpleCase(String expression) {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.havingSimpleCase(expression);
    }

    @Override
    public HavingOrBuilder<X> havingOr() {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.havingOr();
    }

    @Override
    public SubqueryInitiator<X> havingExists() {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.havingExists();
    }

    @Override
    public SubqueryInitiator<X> havingNotExists() {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.havingNotExists();
    }

    @Override
    public SubqueryBuilder<X> havingExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.havingExists(criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<X> havingNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.havingNotExists(criteriaBuilder);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<X>> havingSubquery() {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.havingSubquery();
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<X>> havingSubquery(String subqueryAlias, String expression) {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.havingSubquery(subqueryAlias, expression);
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<X>> havingSubqueries(String expression) {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.havingSubqueries(expression);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<X>> havingSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.havingSubquery(criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<X>> havingSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.havingSubquery(subqueryAlias, expression, criteriaBuilder);
    }

    @Override
    public X setHavingExpression(String expression) {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.setHavingExpression(expression);
    }

    @Override
    public MultipleSubqueryInitiator<X> setHavingExpressionSubqueries(String expression) {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling having() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.setHavingExpressionSubqueries(expression);
    }

    @Override
    public X groupBy(String... paths) {
        if (explicitPaginatedIdentifier) {
            throw new IllegalStateException("Cannot add a GROUP BY clause when paginating by the expressions [" + expressionString(getIdentifierExpressions()) + "]");
        }
        return super.groupBy(paths);
    }

    @Override
    public X groupBy(String expression) {
        if (explicitPaginatedIdentifier) {
            throw new IllegalStateException("Cannot add a GROUP BY clause when paginating by the expressions [" + expressionString(getIdentifierExpressions()) + "]");
        }
        return super.groupBy(expression);
    }
}
