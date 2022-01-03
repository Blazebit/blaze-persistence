/*
 * Copyright 2014 - 2022 Blazebit.
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
import com.blazebit.persistence.CriteriaBuilder;
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
import com.blazebit.persistence.impl.function.alias.AliasFunction;
import com.blazebit.persistence.impl.function.coltrunc.ColumnTruncFunction;
import com.blazebit.persistence.impl.function.count.AbstractCountFunction;
import com.blazebit.persistence.impl.function.countwrapper.CountWrapperFunction;
import com.blazebit.persistence.impl.function.entity.EntityFunction;
import com.blazebit.persistence.impl.function.limit.LimitFunction;
import com.blazebit.persistence.impl.function.nullsubquery.NullSubqueryFunction;
import com.blazebit.persistence.impl.keyset.KeysetMode;
import com.blazebit.persistence.impl.keyset.KeysetPaginationHelper;
import com.blazebit.persistence.impl.keyset.SimpleKeysetLink;
import com.blazebit.persistence.impl.query.CTENode;
import com.blazebit.persistence.impl.query.CustomQuerySpecification;
import com.blazebit.persistence.impl.query.CustomSQLTypedQuery;
import com.blazebit.persistence.impl.query.EntityFunctionNode;
import com.blazebit.persistence.impl.query.QuerySpecification;
import com.blazebit.persistence.impl.query.TypedQueryWrapper;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.StringLiteral;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.parser.util.TypeUtils;
import com.blazebit.persistence.spi.AttributeAccessor;
import com.blazebit.persistence.spi.JpaMetamodelAccessor;

import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.blazebit.persistence.parser.util.JpaMetamodelUtils.ATTRIBUTE_NAME_COMPARATOR;

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
    protected static final Set<ClauseType> OBJECT_QUERY_WITHOUT_GROUP_BY_EXCLUSIONS = EnumSet.of(ClauseType.GROUP_BY);
    protected static final Set<ClauseType> COUNT_QUERY_CLAUSE_EXCLUSIONS = EnumSet.of(ClauseType.ORDER_BY, ClauseType.SELECT);
    protected static final Set<ClauseType> COUNT_QUERY_GROUP_BY_CLAUSE_EXCLUSIONS = EnumSet.of(ClauseType.ORDER_BY, ClauseType.SELECT, ClauseType.GROUP_BY);
    protected static final Set<ClauseType> ID_QUERY_CLAUSE_EXCLUSIONS = EnumSet.of(ClauseType.SELECT);
    protected static final Set<ClauseType> ID_QUERY_GROUP_BY_CLAUSE_EXCLUSIONS = EnumSet.of(ClauseType.SELECT, ClauseType.GROUP_BY);

    protected long cachedMaximumCount;
    protected String cachedCountQueryString;
    protected String cachedExternalCountQueryString;
    protected Set<JoinNode> cachedIdentifierExpressionsToUseNonRootJoinNodes;

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
    private JoinNodeGathererVisitor joinNodeGathererVisitor;

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
    protected void prepareForModification(ClauseType changedClause) {
        super.prepareForModification(changedClause);
        cachedMaximumCount = Long.MAX_VALUE;
        cachedCountQueryString = null;
        cachedExternalCountQueryString = null;
        cachedIdentifierExpressionsToUseNonRootJoinNodes = null;
        uniqueIdentifierExpressions = null;
    }

    @Override
    AbstractCommonQueryBuilder<T, X, Z, W, FinalSetReturn> copy(QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        throw new UnsupportedOperationException("This should only be used on CTEs!");
    }

    @Override
    public <Y> FullQueryBuilder<Y, ?> copy(Class<Y> resultClass) {
        return copyCriteriaBuilder(resultClass, true);
    }

    @Override
    public <Y> CriteriaBuilderImpl<Y> copyCriteriaBuilder(Class<Y> resultClass, boolean copyOrderBy) {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling copy() on a CriteriaBuilder that was transformed to a PaginatedCriteriaBuilder is not allowed.");
        }
        prepareAndCheck();
        MainQuery mainQuery = cbf.createMainQuery(getEntityManager());
        mainQuery.copyConfiguration(this.mainQuery.getQueryConfiguration());
        CriteriaBuilderImpl<Y> newBuilder = new CriteriaBuilderImpl<Y>(mainQuery, true, resultClass, null);
        newBuilder.fromClassExplicitlySet = true;

        newBuilder.applyFrom(this, true, true, false, copyOrderBy, Collections.<ClauseType>emptySet(), Collections.<JoinNode>emptySet(), new IdentityHashMap<JoinManager, JoinManager>(), ExpressionCopyContext.EMPTY);

        return newBuilder;
    }

    @Override
    public CriteriaBuilder<Object[]> createPageIdQuery(int firstResult, int maxResults, String identifierExpression) {
        return createPageIdQuery(null, firstResult, maxResults, getIdentifierExpressionsToUse(identifierExpression, null));
    }

    @Override
    public CriteriaBuilder<Object[]> createPageIdQuery(KeysetPage keysetPage, int firstResult, int maxResults, String identifierExpression) {
        return createPageIdQuery(keysetPage, firstResult, maxResults, getIdentifierExpressionsToUse(identifierExpression, null));
    }

    @Override
    public CriteriaBuilder<Object[]> createPageIdQuery(int firstResult, int maxResults, String identifierExpression, String... identifierExpressions) {
        return createPageIdQuery(null, firstResult, maxResults, getIdentifierExpressionsToUse(identifierExpression, identifierExpressions));
    }

    @Override
    public CriteriaBuilder<Object[]> createPageIdQuery(KeysetPage keysetPage, int firstResult, int maxResults, String identifierExpression, String... identifierExpressions) {
        return createPageIdQuery(keysetPage, firstResult, maxResults, getIdentifierExpressionsToUse(identifierExpression, identifierExpressions));
    }

    private ResolvedExpression[] getIdentifierExpressionsToUse(String identifierExpression, String[] identifierExpressions) {
        ResolvedExpression[] resolvedExpressions = getIdentifierExpressions(identifierExpression, identifierExpressions);
        ResolvedExpression[] uniqueResolvedExpressions = functionalDependencyAnalyzerVisitor.getFunctionalDependencyRootExpressions(whereManager.rootPredicate.getPredicate(), resolvedExpressions, joinManager.getRoots().get(0));
        if (uniqueResolvedExpressions != null) {
            return uniqueResolvedExpressions;
        }
        return resolvedExpressions;
    }

    protected CriteriaBuilder<Object[]> createPageIdQuery(KeysetPage keysetPage, int firstResult, int maxResults, ResolvedExpression[] identifierExpressionsToUse) {
        prepareAndCheck();
        MainQuery mainQuery = cbf.createMainQuery(getEntityManager());
        mainQuery.copyConfiguration(this.mainQuery.getQueryConfiguration());
        CriteriaBuilderImpl<Object[]> newBuilder = new CriteriaBuilderImpl<>(mainQuery, true, Object[].class, null);
        newBuilder.fromClassExplicitlySet = true;
        applyPageIdQueryInto(newBuilder, keysetPage, firstResult, maxResults, identifierExpressionsToUse, false);
        return newBuilder;
    }

    protected void applyPageIdQueryInto(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> newBuilder, KeysetPage keysetPage, int firstResult, int maxResults, ResolvedExpression[] identifierExpressionsToUse, boolean withAlias) {
        ExpressionCopyContext expressionCopyContext = newBuilder.applyFrom(this, true, false, false, false, ID_QUERY_GROUP_BY_CLAUSE_EXCLUSIONS, getIdentifierExpressionsToUseNonRootJoinNodes(identifierExpressionsToUse), new IdentityHashMap<JoinManager, JoinManager>(), ExpressionCopyContext.EMPTY);
        newBuilder.setFirstResult(firstResult);
        newBuilder.setMaxResults(maxResults);

        // Paginated criteria builders always need the last order by expression to be unique
        List<OrderByExpression> orderByExpressions = orderByManager.getOrderByExpressions(false, whereManager.rootPredicate.getPredicate(), hasGroupBy ? Arrays.asList(getIdentifierExpressions()) : Collections.<ResolvedExpression>emptyList(), null);
        if (!orderByExpressions.get(orderByExpressions.size() - 1).isResultUnique()) {
            throw new IllegalStateException("The order by items of the query builder are not guaranteed to produce unique tuples! Consider also ordering by the entity identifier!");
        }

        if (keysetPage != null) {
            KeysetMode keysetMode = KeysetPaginationHelper.getKeysetMode(keysetPage, null, firstResult, maxResults);
            if (keysetMode == KeysetMode.NONE) {
                newBuilder.keysetManager.setKeysetLink(null);
            } else if (keysetMode == KeysetMode.NEXT) {
                newBuilder.keysetManager.setKeysetLink(new SimpleKeysetLink(keysetPage.getHighest(), keysetMode));
            } else {
                newBuilder.keysetManager.setKeysetLink(new SimpleKeysetLink(keysetPage.getLowest(), keysetMode));
            }
            newBuilder.keysetManager.initialize(orderByExpressions);
        }

        // Applying order by items needs special care for page id queries because we have to re-alias the items to avoid collisions
        Map<String, Integer> identifierExpressionStringMap = new HashMap<>(identifierExpressionsToUse.length);
        for (int i = 0; i < identifierExpressionsToUse.length; i++) {
            identifierExpressionStringMap.put(identifierExpressionsToUse[i].getExpressionString(), i);
        }
        String[] identifierToUseSelectAliases = newBuilder.orderByManager.applyFrom(orderByManager, identifierExpressionStringMap);

        if (withAlias) {
            for (int i = 0; i < identifierExpressionsToUse.length; i++) {
                List<Expression> args = new ArrayList<>(2);
                args.add(identifierExpressionsToUse[i].getExpression().copy(expressionCopyContext));
                args.add(new StringLiteral(ColumnTruncFunction.SYNTHETIC_COLUMN_PREFIX + i));
                newBuilder.selectManager.select(new FunctionExpression(AliasFunction.FUNCTION_NAME, args), identifierToUseSelectAliases[i]);
            }
        } else {
            for (int i = 0; i < identifierExpressionsToUse.length; i++) {
                newBuilder.selectManager.select(identifierExpressionsToUse[i].getExpression().copy(expressionCopyContext), identifierToUseSelectAliases[i]);
            }
        }
    }

    private String getCountQueryStringWithoutCheck(long maximumCount) {
        if (cachedMaximumCount != maximumCount) {
            cachedMaximumCount = maximumCount;
            cachedCountQueryString = null;
            cachedExternalCountQueryString = null;
        }
        if (cachedCountQueryString == null) {
            cachedCountQueryString = buildPageCountQueryString(false, true, cachedMaximumCount);
        }

        return cachedCountQueryString;
    }

    private String getExternalCountQueryString(long maximumCount) {
        if (cachedMaximumCount != maximumCount) {
            cachedMaximumCount = maximumCount;
            cachedCountQueryString = null;
            cachedExternalCountQueryString = null;
        }
        if (cachedExternalCountQueryString == null) {
            cachedExternalCountQueryString = buildPageCountQueryString(true, true, cachedMaximumCount);
        }

        return cachedExternalCountQueryString;
    }

    protected String buildPageCountQueryString(boolean externalRepresentation, boolean countAll, long maximumCount) {
        StringBuilder sbSelectFrom = new StringBuilder();
        if (externalRepresentation && isMainQuery) {
            mainQuery.cteManager.buildClause(sbSelectFrom);
        }
        if (useCountWrapper(countAll)) {
            if (externalRepresentation) {
                sbSelectFrom.append("SELECT COUNT(*) FROM (");
                buildBaseQueryString(sbSelectFrom, externalRepresentation, null, true);
                if (maximumCount != Long.MAX_VALUE) {
                    sbSelectFrom.append(" LIMIT ").append(maximumCount);
                }
                sbSelectFrom.append(')');
            } else {
                buildBaseQueryString(sbSelectFrom, externalRepresentation, null, true);
            }
        } else {
            buildPageCountQueryString(sbSelectFrom, externalRepresentation, countAll && !hasGroupBy && !selectManager.isDistinct(), maximumCount);
        }
        return sbSelectFrom.toString();
    }

    protected final boolean useCountWrapper(boolean countAll) {
        return isComplexCountQuery() || !mainQuery.dbmsDialect.supportsCountTuple() && countAll && (hasGroupBy || selectManager.isDistinct());
    }

    protected final boolean isComplexCountQuery() {
        return !havingManager.isEmpty() || hasGroupBy && selectManager.isDistinct() || hasLimit() && !(this instanceof PaginatedCriteriaBuilderImpl<?>);
    }

    protected final String getDualNodeAlias() {
        return "dual_";
    }

    protected final JoinNode createDualNode() {
        return JoinNode.createSimpleValuesRootNode(mainQuery, Long.class, 1, new JoinAliasInfo(getDualNodeAlias(), getDualNodeAlias(), false, true, null));
    }

    protected final void buildPageCountQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation, boolean countAll, long maximumCount) {
        sbSelectFrom.append("SELECT ");
        int countStartIdx = sbSelectFrom.length();
        int countEndIdx;
        boolean isResultUnique;

        if (maximumCount != Long.MAX_VALUE) {
            // bounded counting
            Set<JoinNode> alwaysIncludedNodes = getIdentifierExpressionsToUseNonRootJoinNodes();
            Set<ClauseType> clauseExclusions = countAll ? NO_CLAUSE_EXCLUSION : COUNT_QUERY_GROUP_BY_CLAUSE_EXCLUSIONS;
            List<JoinNode> entityFunctions = joinManager.getEntityFunctions(clauseExclusions, true, alwaysIncludedNodes);
            List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(null, entityFunctions);

            JoinNode valuesNode = createDualNode();
            if (externalRepresentation) {
                sbSelectFrom.append("COUNT(*)");
                appendPageCountQueryStringExtensions(sbSelectFrom);
                sbSelectFrom.append(" FROM ");
            } else {
                sbSelectFrom.append(mainQuery.jpaProvider.getCustomFunctionInvocation(CountWrapperFunction.FUNCTION_NAME, 1));
                sbSelectFrom.append(mainQuery.jpaProvider.getCustomFunctionInvocation(LimitFunction.FUNCTION_NAME, 1));

                for (int i = 0; i < entityFunctionNodes.size(); i++) {
                    sbSelectFrom.append(mainQuery.jpaProvider.getCustomFunctionInvocation(EntityFunction.FUNCTION_NAME, 1));
                }
            }

            // Start of the content of the count query wrapper
            sbSelectFrom.append("(SELECT ");
            countStartIdx = sbSelectFrom.length();
            String aliasFunctionInvocation = mainQuery.jpaProvider.getCustomFunctionInvocation(AliasFunction.FUNCTION_NAME, 1);
            if (countAll) {
                sbSelectFrom.append(aliasFunctionInvocation);
                sbSelectFrom.append("1, 'c')");
                countEndIdx = sbSelectFrom.length() - 1;
                isResultUnique = true;
            } else {
                sbSelectFrom.append("DISTINCT ");
                isResultUnique = appendIdentifierExpressions(sbSelectFrom, true);
                countEndIdx = sbSelectFrom.length() - 1;
            }

            List<String> whereClauseConjuncts = new ArrayList<>();
            List<String> optionalWhereClauseConjuncts = new ArrayList<>();
            // The count query does not have any fetch owners
            Set<JoinNode> countNodesToFetch = Collections.emptySet();

            if (countAll) {
                joinManager.buildClause(sbSelectFrom, clauseExclusions, null, false, externalRepresentation, false, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, countNodesToFetch, Collections.<JoinNode>emptySet(), null, true);
                whereManager.buildClause(sbSelectFrom, whereClauseConjuncts, optionalWhereClauseConjuncts);
            } else {
                // Collect usage of collection join nodes to optimize away the count distinct
                // Note that we always exclude the nodes with group by dependency. We consider just the ones from the identifiers
                Set<JoinNode> identifierExpressionsToUseNonRootJoinNodes = getIdentifierExpressionsToUseNonRootJoinNodes();
                Set<JoinNode> collectionJoinNodes = joinManager.buildClause(sbSelectFrom, clauseExclusions, null, true, externalRepresentation, true, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, countNodesToFetch, identifierExpressionsToUseNonRootJoinNodes, null, true);
                boolean hasCollectionJoinUsages = collectionJoinNodes.size() > 0;

                whereManager.buildClause(sbSelectFrom, whereClauseConjuncts, optionalWhereClauseConjuncts);

                // Instead of a count distinct, we render a 1 if we have no collection joins and the identifier expression is result unique
                // It is result unique when it contains the query root primary key or a unique key that of a uniqueness preserving association of that
                if (!hasCollectionJoinUsages && isResultUnique) {
                    for (int i = countStartIdx, j = 0; i < countEndIdx; i++, j++) {
                        sbSelectFrom.setCharAt(i, ' ');
                    }
                    String suffix = "1, 'c'";
                    sbSelectFrom.replace(countEndIdx - (aliasFunctionInvocation.length() + suffix.length()), countEndIdx - suffix.length(), aliasFunctionInvocation);
                    sbSelectFrom.replace(countEndIdx - suffix.length(), countEndIdx, suffix);
                }
            }

            if (externalRepresentation) {
                sbSelectFrom.append(" LIMIT ").append(maximumCount).append(')');
            } else {
                if (!mainQuery.dbmsDialect.supportsLimitWithoutOrderBy()) {
                    sbSelectFrom.append(" ORDER BY ");
                    sbSelectFrom.append(mainQuery.jpaProvider.getCustomFunctionInvocation(NullSubqueryFunction.FUNCTION_NAME, 0));
                    sbSelectFrom.append(')');
                }
                // Close subquery
                sbSelectFrom.append(')');

                finishEntityFunctionNodes(sbSelectFrom, entityFunctionNodes);

                // Limit
                sbSelectFrom.append(", ").append(maximumCount).append(")");
                // Count wrapper
                sbSelectFrom.append(")");

                appendPageCountQueryStringExtensions(sbSelectFrom);
                sbSelectFrom.append(" FROM ").append(valuesNode.getValueType().getName()).append(" ").append(valuesNode.getAlias());
                // Values predicate
                sbSelectFrom.append(" WHERE ");
                joinManager.renderPlaceholderRequiringPredicate(sbSelectFrom, valuesNode, valuesNode.getAlias(), externalRepresentation, true);
            }
        } else {
            if (countAll) {
                if (mainQuery.jpaProvider.supportsCountStar()) {
                    sbSelectFrom.append("COUNT(*)");
                } else if (mainQuery.jpaProvider.supportsCustomFunctions()) {
                    sbSelectFrom.append(mainQuery.jpaProvider.getCustomFunctionInvocation("count_star", 0)).append(')');
                } else {
                    sbSelectFrom.append("COUNT(");
                    appendIdentifierExpressions(sbSelectFrom, false);
                    sbSelectFrom.append(")");
                }
                countEndIdx = sbSelectFrom.length() - 1;
                isResultUnique = true;
            } else if (mainQuery.jpaProvider.supportsCustomFunctions()) {
                sbSelectFrom.append(mainQuery.jpaProvider.getCustomFunctionInvocation(AbstractCountFunction.FUNCTION_NAME, 1));
                sbSelectFrom.append("'DISTINCT',");

                if (selectManager.isDistinct()) {
                    selectManager.buildSelectItems(sbSelectFrom, false, externalRepresentation, false);
                    isResultUnique = false;
                } else {
                    isResultUnique = appendIdentifierExpressions(sbSelectFrom, false);
                }

                sbSelectFrom.append(")");
                countEndIdx = sbSelectFrom.length() - 1;

                appendPageCountQueryStringExtensions(sbSelectFrom);
            } else {
                sbSelectFrom.append("COUNT(");
                sbSelectFrom.append("DISTINCT ");

                if (selectManager.isDistinct()) {
                    selectManager.buildSelectItems(sbSelectFrom, false, externalRepresentation, false);
                    isResultUnique = false;
                } else {
                    isResultUnique = appendIdentifierExpressions(sbSelectFrom, false);
                }

                sbSelectFrom.append(")");
                countEndIdx = sbSelectFrom.length() - 1;

                appendPageCountQueryStringExtensions(sbSelectFrom);
            }

            List<String> whereClauseConjuncts = new ArrayList<>();
            List<String> optionalWhereClauseConjuncts = new ArrayList<>();
            // The count query does not have any fetch owners
            Set<JoinNode> countNodesToFetch = Collections.emptySet();

            if (countAll) {
                joinManager.buildClause(sbSelectFrom, NO_CLAUSE_EXCLUSION, null, false, externalRepresentation, false, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, countNodesToFetch, Collections.<JoinNode>emptySet(), null, true);
                whereManager.buildClause(sbSelectFrom, whereClauseConjuncts, optionalWhereClauseConjuncts);
            } else {
                // Collect usage of collection join nodes to optimize away the count distinct
                // Note that we always exclude the nodes with group by dependency. We consider just the ones from the identifiers
                Set<JoinNode> identifierExpressionsToUseNonRootJoinNodes = getIdentifierExpressionsToUseNonRootJoinNodes();
                Set<JoinNode> collectionJoinNodes = joinManager.buildClause(sbSelectFrom, COUNT_QUERY_GROUP_BY_CLAUSE_EXCLUSIONS, null, true, externalRepresentation, true, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, countNodesToFetch, identifierExpressionsToUseNonRootJoinNodes, null, true);
                boolean hasCollectionJoinUsages = collectionJoinNodes.size() > 0;

                whereManager.buildClause(sbSelectFrom, whereClauseConjuncts, optionalWhereClauseConjuncts);

                // Instead of a count distinct, we render a count(*) if we have no collection joins and the identifier expression is result unique
                // It is result unique when it contains the query root primary key or a unique key that of a uniqueness preserving association of that
                if (!hasCollectionJoinUsages && isResultUnique) {
                    if (mainQuery.jpaProvider.supportsCustomFunctions()) {
                        String countStar;
                        if (mainQuery.jpaProvider.supportsCountStar()) {
                            countStar = "COUNT(*";
                        } else {
                            countStar = mainQuery.jpaProvider.getCustomFunctionInvocation("count_star", 0);
                        }
                        for (int i = countStartIdx, j = 0; i < countEndIdx; i++, j++) {
                            if (j < countStar.length()) {
                                sbSelectFrom.setCharAt(i, countStar.charAt(j));
                            } else {
                                sbSelectFrom.setCharAt(i, ' ');
                            }
                        }
                    } else {
                        // Strip off the distinct part
                        int i = countStartIdx + "COUNT(".length();
                        countEndIdx = i + "DISTINCT ".length();
                        for (; i < countEndIdx; i++) {
                            sbSelectFrom.setCharAt(i, ' ');
                        }

                    }
                }
            }
        }
    }

    protected void finishEntityFunctionNodes(StringBuilder sbSelectFrom, List<EntityFunctionNode> entityFunctionNodes) {
        for (EntityFunctionNode node : entityFunctionNodes) {
            String subquery = node.getSubquery();
            String aliases = node.getAliases();
            String syntheticPredicate = node.getSyntheticPredicate();

            // TODO: this is a hibernate specific integration detail
            // Replace the subview subselect that is generated for this subselect
            sbSelectFrom.append(",'");
            sbSelectFrom.append(node.getEntityName());
            sbSelectFrom.append("',");
            TypeUtils.STRING_CONVERTER.appendTo(subquery, sbSelectFrom);
            sbSelectFrom.append(",'");
            if (aliases != null) {
                sbSelectFrom.append(aliases);
            }
            sbSelectFrom.append("','");
            if (syntheticPredicate != null) {
                sbSelectFrom.append(syntheticPredicate);
            }
            sbSelectFrom.append("')");
        }
    }

    protected void appendPageCountQueryStringExtensions(StringBuilder sbSelectFrom) {
    }

    protected boolean appendIdentifierExpressions(StringBuilder sbSelectFrom, boolean alias) {
        boolean isResultUnique;
        ResolvedExpression[] identifierExpressions = getIdentifierExpressions();
        ResolvedExpression[] resultUniqueExpressions = getUniqueIdentifierExpressions();

        if (resultUniqueExpressions == null) {
            isResultUnique = false;
        } else {
            // We only render the identifiers that are necessary to make it unique
            identifierExpressions = resultUniqueExpressions;
            isResultUnique = resultUniqueExpressions == entityIdentifierExpressions || functionalDependencyAnalyzerVisitor.isResultUnique();
        }

        queryGenerator.setQueryBuffer(sbSelectFrom);
        for (int i = 0; i < identifierExpressions.length; i++) {
            if (alias) {
                sbSelectFrom.append(mainQuery.jpaProvider.getCustomFunctionInvocation(AliasFunction.FUNCTION_NAME, 1));
            }
            identifierExpressions[i].getExpression().accept(queryGenerator);
            if (alias) {
                sbSelectFrom.append(", 'c").append(i).append("')");
            }
            sbSelectFrom.append(", ");
        }
        sbSelectFrom.setLength(sbSelectFrom.length() - 2);
        return isResultUnique;
    }

    protected ResolvedExpression[] getUniqueIdentifierExpressions() {
        if (uniqueIdentifierExpressions == null) {
            ResolvedExpression[] identifierExpressions = getIdentifierExpressions();
            // Fast path, if we see that the identifier expressions are the entity identifier expressions, we don't need to check uniqueness
            if (identifierExpressions == entityIdentifierExpressions) {
                uniqueIdentifierExpressions = identifierExpressions;
            } else {
                uniqueIdentifierExpressions = functionalDependencyAnalyzerVisitor.getFunctionalDependencyRootExpressions(whereManager.rootPredicate.getPredicate(), identifierExpressions, joinManager.getRoots().get(0));
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

    protected Set<JoinNode> getIdentifierExpressionsToUseNonRootJoinNodes() {
        if (cachedIdentifierExpressionsToUseNonRootJoinNodes == null) {
            cachedIdentifierExpressionsToUseNonRootJoinNodes = getIdentifierExpressionsToUseNonRootJoinNodes(getIdentifierExpressionsToUse());
        }

        return cachedIdentifierExpressionsToUseNonRootJoinNodes;
    }

    protected Set<JoinNode> getIdentifierExpressionsToUseNonRootJoinNodes(ResolvedExpression[] identifierExpressionsToUse) {
        if (joinNodeGathererVisitor == null) {
            joinNodeGathererVisitor = new JoinNodeGathererVisitor();
        }
        Set<JoinNode> joinNodes = joinNodeGathererVisitor.collectNonRootJoinNodes(identifierExpressionsToUse);
        // Remove join nodes that use non-optional one-to-one associations
        Iterator<JoinNode> iterator = joinNodes.iterator();
        OUTER: while (iterator.hasNext()) {
            JoinNode joinNode = iterator.next();
            JoinTreeNode parentTreeNode;
            while ((parentTreeNode = joinNode.getParentTreeNode()) != null) {
                if (parentTreeNode.isOptional() || parentTreeNode.getAttribute().getPersistentAttributeType() != Attribute.PersistentAttributeType.ONE_TO_ONE) {
                    continue OUTER;
                }
                joinNode = joinNode.getParent();
            }
            iterator.remove();
        }

        return joinNodes;
    }

    protected ResolvedExpression[] getIdentifierExpressions() {
        if (hasGroupBy) {
            return getGroupByIdentifierExpressions();
        }

        return getQueryRootEntityIdentifierExpressions();
    }

    @Override
    public String getCountQueryString() {
        prepareAndCheck();
        return getExternalCountQueryString(Long.MAX_VALUE);
    }

    @Override
    public String getCountQueryString(long maximumCount) {
        prepareAndCheck();
        return getExternalCountQueryString(maximumCount);
    }

    @Override
    public TypedQuery<Long> getCountQuery() {
        prepareAndCheck();
        return getCountQuery(getCountQueryStringWithoutCheck(Long.MAX_VALUE), useCountWrapper(true));
    }

    @Override
    public TypedQuery<Long> getCountQuery(long maximumCount) {
        prepareAndCheck();
        return getCountQuery(getCountQueryStringWithoutCheck(maximumCount), useCountWrapper(true));
    }

    protected TypedQuery<Long> getCountQuery(String countQueryString, boolean useCountWrapper) {
        // We can only use the query directly if we have no ctes, entity functions or hibernate bugs
        Set<JoinNode> keyRestrictedLeftJoins = getKeyRestrictedLeftJoins();
        Set<JoinNode> alwaysIncludedNodes = getIdentifierExpressionsToUseNonRootJoinNodes();
        List<JoinNode> entityFunctions = null;
        boolean normalQueryMode = !useCountWrapper && (!isMainQuery || (!mainQuery.cteManager.hasCtes() && (entityFunctions = joinManager.getEntityFunctions(COUNT_QUERY_GROUP_BY_CLAUSE_EXCLUSIONS, true, alwaysIncludedNodes)).isEmpty() && keyRestrictedLeftJoins.isEmpty()));

        Collection<Parameter<?>> parameters;
        Map<String, String> valuesParameters;
        Map<String, ValuesParameterBinder> valuesBinders;
        JoinNode dualNode = null;
        if (cachedMaximumCount == Long.MAX_VALUE) {
            if (normalQueryMode && isEmpty(keyRestrictedLeftJoins, COUNT_QUERY_CLAUSE_EXCLUSIONS)) {
                TypedQuery<Long> countQuery = em.createQuery(countQueryString, Long.class);
                if (isCacheable()) {
                    mainQuery.jpaProvider.setCacheable(countQuery);
                }
                parameterManager.parameterizeQuery(countQuery);
                return parameterManager.getCriteriaNameMapping() == null ? countQuery : new TypedQueryWrapper<>(countQuery, parameterManager.getCriteriaNameMapping());
            }
            parameters = (Collection<Parameter<?>>) (Collection<?>) parameterManager.getParameterImpls();
            valuesParameters = parameterManager.getValuesParameters();
            valuesBinders = parameterManager.getValuesBinders();
        } else {
            parameters = new ArrayList<>(parameterManager.getParameters());
            valuesParameters = new HashMap<>(parameterManager.getValuesParameters());
            valuesBinders = parameterManager.getValuesBinders();
            dualNode = createDualNode();
            entityFunctions = new ArrayList<>();
            entityFunctions.add(dualNode);
            String valueParameterName = dualNode.getAlias() + "_value_0";
            String[][] parameterNames = new String[1][1];
            parameterNames[0][0] = valueParameterName;
            ParameterManager.ValuesParameterWrapper valuesParameterWrapper = new ParameterManager.ValuesParameterWrapper(dualNode.getJavaType(), parameterNames, new AttributeAccessor[1]);
            parameters.add(new ParameterManager.ParameterImpl<Object>(dualNode.getAlias(), false, null, null, valuesParameterWrapper));
            valuesParameters.put(valueParameterName, dualNode.getAlias());
            valuesBinders.put(dualNode.getAlias(), valuesParameterWrapper.getBinder());
        }
        if (entityFunctions == null) {
            entityFunctions = joinManager.getEntityFunctions(COUNT_QUERY_GROUP_BY_CLAUSE_EXCLUSIONS, true, alwaysIncludedNodes);
        }

        Query baseQuery = em.createQuery(countQueryString);
        Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery);
        String limit = null;
        String offset = null;
        if (firstResult != 0) {
            offset = Integer.toString(firstResult);
        }
        if (maxResults != Integer.MAX_VALUE) {
            limit = Integer.toString(maxResults);
        }
        List<String> keyRestrictedLeftJoinAliases = getKeyRestrictedLeftJoinAliases(baseQuery, keyRestrictedLeftJoins, COUNT_QUERY_CLAUSE_EXCLUSIONS);
        List<EntityFunctionNode> entityFunctionNodes;
        if (dualNode == null) {
            entityFunctionNodes = getEntityFunctionNodes(baseQuery, entityFunctions);
        } else {
            entityFunctionNodes = getEntityFunctionNodes(baseQuery, entityFunctions, Collections.<JoinNode>emptyList(), false);
        }
        boolean shouldRenderCteNodes = renderCteNodes(false);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(false) : Collections.EMPTY_LIST;
        QuerySpecification querySpecification = new CustomQuerySpecification(
                this, baseQuery, parameters, parameterListNames, limit, offset, keyRestrictedLeftJoinAliases, entityFunctionNodes,
                mainQuery.cteManager.isRecursive(), ctes, shouldRenderCteNodes, mainQuery.getQueryConfiguration().isQueryPlanCacheEnabled(),
                useCountWrapper ? getCountExampleQuery() : null
        );

        CustomSQLTypedQuery<Long> countQuery = new CustomSQLTypedQuery<>(
                querySpecification,
                baseQuery,
                parameterManager.getCriteriaNameMapping(),
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

    protected Query getCountExampleQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        if (mainQuery.jpaProvider.supportsCountStar()) {
            sb.append("COUNT(*)");
        } else if (mainQuery.jpaProvider.supportsCustomFunctions()) {
            sb.append(mainQuery.jpaProvider.getCustomFunctionInvocation("count_star", 0)).append(')');
        } else {
            sb.append("COUNT(e)");
        }
        sb.append(" FROM ValuesEntity e");

        String exampleQueryString = sb.toString();
        return em.createQuery(exampleQueryString);
    }

    @Override
    public PaginatedCriteriaBuilder<T> page(int firstRow, int pageSize) {
        return pageBy(firstRow, pageSize, (ResolvedExpression[]) null);
    }

    @Override
    public PaginatedCriteriaBuilder<T> page(Object entityId, int pageSize) {
        return pageByAndNavigate(entityId, pageSize, getQueryRootEntityIdentifierExpressions());
    }
    @Override
    public PaginatedCriteriaBuilder<T> pageAndNavigate(Object entityId, int pageSize) {
        return pageByAndNavigate(entityId, pageSize, getQueryRootEntityIdentifierExpressions());
    }

    @Override
    public PaginatedCriteriaBuilder<T> page(KeysetPage keysetPage, int firstRow, int pageSize) {
        return pageBy(keysetPage, firstRow, pageSize, (ResolvedExpression[]) null);
    }

    @Override
    public PaginatedCriteriaBuilder<T> pageBy(int firstRow, int pageSize, String identifierExpression) {
        return pageBy(firstRow, pageSize, getIdentifierExpressions(identifierExpression, null));
    }

    @Override
    public PaginatedCriteriaBuilder<T> pageByAndNavigate(Object entityId, int pageSize, String identifierExpression) {
        return pageByAndNavigate(entityId, pageSize, getIdentifierExpressions(identifierExpression, null));
    }

    @Override
    public PaginatedCriteriaBuilder<T> pageBy(KeysetPage keysetPage, int firstRow, int pageSize, String identifierExpression) {
        return pageBy(keysetPage, firstRow, pageSize, getIdentifierExpressions(identifierExpression, null));
    }

    @Override
    public PaginatedCriteriaBuilder<T> pageBy(int firstRow, int pageSize, String identifierExpression, String... identifierExpressions) {
        return pageBy(firstRow, pageSize, getIdentifierExpressions(identifierExpression, identifierExpressions));
    }

    @Override
    public PaginatedCriteriaBuilder<T> pageByAndNavigate(Object entityId, int pageSize, String identifierExpression, String... identifierExpressions) {
        return pageByAndNavigate(entityId, pageSize, getIdentifierExpressions(identifierExpression, identifierExpressions));
    }

    @Override
    public PaginatedCriteriaBuilder<T> pageBy(KeysetPage keysetPage, int firstRow, int pageSize, String identifierExpression, String... identifierExpressions) {
        return pageBy(keysetPage, firstRow, pageSize, getIdentifierExpressions(identifierExpression, identifierExpressions));
    }

    protected ResolvedExpression[] getQueryRootEntityIdentifierExpressions() {
        if (entityIdentifierExpressions == null) {
            JoinNode rootNode = joinManager.getRootNodeOrFail("Paginated criteria builders do not support multiple from clause elements!");
            Set<SingularAttribute<?, ?>> idAttributes = JpaMetamodelUtils.getIdAttributes(mainQuery.metamodel.entity(rootNode.getJavaType()));
            @SuppressWarnings("unchecked")
            List<ResolvedExpression> identifierExpressions = new ArrayList<>(idAttributes.size());
            StringBuilder sb = new StringBuilder();
            addAttributes("", idAttributes, identifierExpressions, sb, rootNode);
            entityIdentifierExpressions = identifierExpressions.toArray(new ResolvedExpression[identifierExpressions.size()]);
        }
        return entityIdentifierExpressions;
    }

    private void addAttributes(String prefix, Set<SingularAttribute<?, ?>> attributes, List<ResolvedExpression> resolvedExpressions, StringBuilder sb, JoinNode rootNode) {
        for (SingularAttribute<?, ?> attribute : attributes) {
            String attributeName;
            if (prefix.isEmpty()) {
                attributeName = attribute.getName();
            } else {
                attributeName = prefix + attribute.getName();
            }

            if (attribute.getType() instanceof EmbeddableType<?>) {
                Set<SingularAttribute<?, ?>> subAttributes = new TreeSet<>(ATTRIBUTE_NAME_COMPARATOR);
                subAttributes.addAll(((EmbeddableType<?>) attribute.getType()).getSingularAttributes());
                addAttributes(attributeName + ".", subAttributes, resolvedExpressions, sb, rootNode);
            } else {
                sb.setLength(0);
                rootNode.appendDeReference(sb, attributeName, false);
                PathExpression expression = (PathExpression) rootNode.createExpression(attributeName);
                JpaMetamodelAccessor jpaMetamodelAccessor = mainQuery.jpaProvider.getJpaMetamodelAccessor();
                expression.setPathReference(new SimplePathReference(rootNode, attributeName, getMetamodel().type(jpaMetamodelAccessor.getAttributePath(getMetamodel(), rootNode.getManagedType(), attributeName).getAttributeClass())));
                resolvedExpressions.add(new ResolvedExpression(sb.toString(), expression));
            }
        }
    }

    private ResolvedExpression[] getIdentifierExpressions(String identifierExpression, String[] identifierExpressions) {
        if (identifierExpression == null) {
            throw new IllegalArgumentException("Invalid null identifier expression passed to page()!");
        }

        // Note: Identifier expressions are inner joined!
        List<ResolvedExpression> resolvedExpressions = new ArrayList<>(identifierExpressions == null ? 1 : identifierExpression.length() + 1);
        Expression expression = expressionFactory.createSimpleExpression(identifierExpression, false);
        joinManager.implicitJoin(expression, true, true, false, null, null, JoinType.INNER, null, new HashSet<String>(), false, false, true, false, false, false);
        StringBuilder sb = new StringBuilder();

        implicitJoinWhereClause();
        functionalDependencyAnalyzerVisitor.clear(whereManager.rootPredicate.getPredicate(), joinManager.getRoots().get(0), true);
        functionalDependencyAnalyzerVisitor.analyzeFormsUniqueTuple(expression);
        queryGenerator.setQueryBuffer(sb);
        if (functionalDependencyAnalyzerVisitor.getSplittedOffExpressions().isEmpty()) {
            sb.setLength(0);
            expression.accept(queryGenerator);
            resolvedExpressions.add(new ResolvedExpression(sb.toString(), expression));
        } else {
            for (Expression splittedOffExpression : functionalDependencyAnalyzerVisitor.getSplittedOffExpressions()) {
                sb.setLength(0);
                splittedOffExpression.accept(queryGenerator);
                resolvedExpressions.add(new ResolvedExpression(sb.toString(), splittedOffExpression));
            }
        }

        if (identifierExpressions != null) {
            for (String expressionString : identifierExpressions) {
                expression = expressionFactory.createSimpleExpression(expressionString, false);
                joinManager.implicitJoin(expression, true, true, false, null, null, JoinType.INNER, null, new HashSet<String>(), false, false, true, false, false, false);
                functionalDependencyAnalyzerVisitor.analyzeFormsUniqueTuple(expression);
                if (functionalDependencyAnalyzerVisitor.getSplittedOffExpressions().isEmpty()) {
                    sb.setLength(0);
                    expression.accept(queryGenerator);
                    ResolvedExpression resolvedExpression = new ResolvedExpression(sb.toString(), expression);
                    if (resolvedExpressions.contains(resolvedExpression)) {
                        throw new IllegalArgumentException("Duplicate identifier expression '" + expressionString + "' in " + Arrays.toString(identifierExpressions) + "!");
                    }
                    resolvedExpressions.add(resolvedExpression);
                } else {
                    for (Expression splittedOffExpression : functionalDependencyAnalyzerVisitor.getSplittedOffExpressions()) {
                        sb.setLength(0);
                        splittedOffExpression.accept(queryGenerator);
                        ResolvedExpression resolvedExpression = new ResolvedExpression(sb.toString(), splittedOffExpression);
                        if (resolvedExpressions.contains(resolvedExpression)) {
                            throw new IllegalArgumentException("Duplicate identifier expression '" + expressionString + "' in " + Arrays.toString(identifierExpressions) + "!");
                        }
                        resolvedExpressions.add(resolvedExpression);
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        ResolvedExpression[] entries = resolvedExpressions.toArray(new ResolvedExpression[resolvedExpressions.size()]);
        if (!functionalDependencyAnalyzerVisitor.isResultUnique()) {
            throw new IllegalArgumentException("The identifier expressions [" + expressionString(entries) + "] do not form a unique tuple which is required for pagination!");
        }

        return entries;
    }

    private PaginatedCriteriaBuilder<T> pageBy(int firstRow, int pageSize, ResolvedExpression[] identifierExpressions) {
        prepareForModification(ClauseType.GROUP_BY);
        if (selectManager.isDistinct()) {
            throw new IllegalStateException("Cannot paginate a DISTINCT query");
        }
        if (!havingManager.isEmpty()) {
            throw new IllegalStateException("Cannot paginate a HAVING query");
        }
        createdPaginatedBuilder = true;
        explicitPaginatedIdentifier = identifierExpressions != null;
        return new PaginatedCriteriaBuilderImpl<T>(this, false, null, firstRow, pageSize, identifierExpressions);
    }

    private PaginatedCriteriaBuilder<T> pageByAndNavigate(Object entityId, int pageSize, ResolvedExpression[] identifierExpressions) {
        prepareForModification(ClauseType.GROUP_BY);
        if (selectManager.isDistinct()) {
            throw new IllegalStateException("Cannot paginate a DISTINCT query");
        }
        if (!havingManager.isEmpty()) {
            throw new IllegalStateException("Cannot paginate a HAVING query");
        }
        checkEntityId(entityId, identifierExpressions);
        createdPaginatedBuilder = true;
        explicitPaginatedIdentifier = identifierExpressions != null;
        return new PaginatedCriteriaBuilderImpl<T>(this, false, entityId, pageSize, identifierExpressions);
    }

    private PaginatedCriteriaBuilder<T> pageBy(KeysetPage keysetPage, int firstRow, int pageSize, ResolvedExpression[] identifierExpressions) {
        prepareForModification(ClauseType.GROUP_BY);
        if (selectManager.isDistinct()) {
            throw new IllegalStateException("Cannot paginate a DISTINCT query");
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
        prepareForModification(ClauseType.SELECT);
        if (clazz == null) {
            throw new NullPointerException("clazz");
        }

        verifyBuilderEnded();
        return selectManager.selectNew(this, clazz);
    }

    @Override
    public <Y> SelectObjectBuilder<? extends FullQueryBuilder<Y, ?>> selectNew(Constructor<Y> constructor) {
        prepareForModification(ClauseType.SELECT);
        if (constructor == null) {
            throw new NullPointerException("constructor");
        }

        verifyBuilderEnded();
        return selectManager.selectNew(this, constructor);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y> FullQueryBuilder<Y, ?> selectNew(ObjectBuilder<Y> objectBuilder) {
        prepareForModification(ClauseType.SELECT);
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
        prepareForModification(ClauseType.JOIN);
        verifyBuilderEnded();
        joinManager.implicitJoin(expressionFactory.createPathExpression(path), true, true, true, null, null, null, null, new HashSet<String>(), false, false, true, false, true, false);
        return (X) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public X fetch(String... paths) {
        if (paths.length != 0) {
            prepareForModification(ClauseType.JOIN);
            verifyBuilderEnded();

            HashSet<String> currentlyResolvingAliases = new HashSet<>();
            for (String path : paths) {
                joinManager.implicitJoin(expressionFactory.createPathExpression(path), true, true, true, null, null, null, null, currentlyResolvingAliases, false, false, true, false, true, false);
            }
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
        prepareForModification(ClauseType.JOIN);
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
        joinManager.join(path, alias, type, fetch, defaultJoin, null);
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

    public X groupByRollup(String... expressions) {
        if (explicitPaginatedIdentifier) {
            throw new IllegalStateException("Cannot use grouping sets when paginating");
        }
        return super.groupByRollup(expressions);
    }

    public X groupByCube(String... expressions) {
        if (explicitPaginatedIdentifier) {
            throw new IllegalStateException("Cannot use grouping sets when paginating");
        }
        return super.groupByCube(expressions);
    }

    public X groupByRollup(String[]... expressions) {
        if (explicitPaginatedIdentifier) {
            throw new IllegalStateException("Cannot use grouping sets when paginating");
        }
        return super.groupByRollup(expressions);
    }

    public X groupByCube(String[]... expressions) {
        if (explicitPaginatedIdentifier) {
            throw new IllegalStateException("Cannot use grouping sets when paginating");
        }
        return super.groupByCube(expressions);
    }

    public X groupByGroupingSets(String[]... expressions) {
        if (explicitPaginatedIdentifier) {
            throw new IllegalStateException("Cannot use grouping sets when paginating");
        }
        return super.groupByGroupingSets(expressions);
    }

}
