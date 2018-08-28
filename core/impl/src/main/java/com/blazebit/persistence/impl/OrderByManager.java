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

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.SimpleQueryGenerator;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.modifier.ExpressionModifier;
import com.blazebit.persistence.impl.transform.ExpressionModifierVisitor;
import com.blazebit.persistence.parser.predicate.CompoundPredicate;
import com.blazebit.persistence.spi.JpaProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class OrderByManager extends AbstractManager<ExpressionModifier> {

    private final EmbeddableSplittingVisitor embeddableSplittingVisitor;
    private final GroupByExpressionGatheringVisitor groupByExpressionGatheringVisitor;
    private final FunctionalDependencyAnalyzerVisitor functionalDependencyAnalyzerVisitor;
    private final List<OrderByInfo> orderByInfos = new ArrayList<OrderByInfo>();
    private final JoinManager joinManager;
    private final AliasManager aliasManager;
    private final EntityMetamodel metamodel;
    private final JpaProvider jpaProvider;

    OrderByManager(ResolvingQueryGenerator queryGenerator, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory, JoinManager joinManager, AliasManager aliasManager,
                   EmbeddableSplittingVisitor embeddableSplittingVisitor, FunctionalDependencyAnalyzerVisitor functionalDependencyAnalyzerVisitor, EntityMetamodel metamodel, JpaProvider jpaProvider, GroupByExpressionGatheringVisitor groupByExpressionGatheringVisitor) {
        super(queryGenerator, parameterManager, subqueryInitFactory);
        this.joinManager = joinManager;
        this.embeddableSplittingVisitor = embeddableSplittingVisitor;
        this.functionalDependencyAnalyzerVisitor = functionalDependencyAnalyzerVisitor;
        this.metamodel = metamodel;
        this.groupByExpressionGatheringVisitor = groupByExpressionGatheringVisitor;
        this.aliasManager = aliasManager;
        this.jpaProvider = jpaProvider;
    }

    void applyFrom(OrderByManager orderByManager) {
        for (OrderByInfo info : orderByManager.orderByInfos) {
            orderBy(subqueryInitFactory.reattachSubqueries(info.getExpression().clone(true), ClauseType.ORDER_BY), info.ascending, info.nullFirst);
        }
    }

    @Override
    public ClauseType getClauseType() {
        return ClauseType.ORDER_BY;
    }

    public boolean containsOrderBySelectAlias(String alias) {
        if (alias == null || orderByInfos.isEmpty()) {
            return false;
        }

        List<OrderByInfo> infos = orderByInfos;
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            final OrderByInfo orderByInfo = infos.get(i);
            String potentialSelectAlias = orderByInfo.getExpressionString();
            if (alias.equals(potentialSelectAlias)) {
                return true;
            }
        }
        return false;
    }

    void splitEmbeddables() {
        List<OrderByInfo> infos = orderByInfos;
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            final OrderByInfo orderByInfo = infos.get(i);
            String potentialSelectAlias = orderByInfo.getExpressionString();
            AliasInfo aliasInfo = aliasManager.getAliasInfo(potentialSelectAlias);
            Expression expr;

            if (aliasInfo instanceof SelectInfo) {
                SelectInfo selectInfo = (SelectInfo) aliasInfo;
                expr = selectInfo.getExpression();
            } else {
                expr = orderByInfo.getExpression();
            }

            List<Expression> splittedOffExpressions = embeddableSplittingVisitor.splitOff(expr);
            if (!splittedOffExpressions.isEmpty()) {
                infos.set(i, new OrderByInfo(splittedOffExpressions.get(0), orderByInfo.ascending, orderByInfo.nullFirst));
                List<OrderByInfo> newOrderByInfos = new ArrayList<>(splittedOffExpressions.size() - 1);
                for (int j = 1; j < splittedOffExpressions.size(); j++) {
                    newOrderByInfos.add(new OrderByInfo(splittedOffExpressions.get(j), orderByInfo.ascending, orderByInfo.nullFirst));
                }
                infos.addAll(i + 1, newOrderByInfos);
                size += newOrderByInfos.size();
            }
        }
    }

    List<OrderByExpression> getOrderByExpressions(boolean hasCollections, CompoundPredicate rootPredicate, Collection<ResolvedExpression> groupByClauses) {
        if (orderByInfos.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> clausesRequiredForResultUniqueness;
        if (groupByClauses.isEmpty()) {
            clausesRequiredForResultUniqueness = null;
        } else {
            clausesRequiredForResultUniqueness = new HashSet<>(groupByClauses.size());
            for (ResolvedExpression groupByClause : groupByClauses) {
                clausesRequiredForResultUniqueness.add(groupByClause.getExpressionString());
            }
        }

        List<OrderByExpression> realExpressions = new ArrayList<>(orderByInfos.size());

        List<OrderByInfo> infos = orderByInfos;
        int size = infos.size();
        boolean resultUnique = false;

        StringBuilder expressionStringBuilder = new StringBuilder();
        queryGenerator.setQueryBuffer(expressionStringBuilder);
        functionalDependencyAnalyzerVisitor.clear(rootPredicate);
        for (int i = 0; i < size; i++) {
            final OrderByInfo orderByInfo = infos.get(i);
            String expressionString = orderByInfo.getExpressionString();
            AliasInfo aliasInfo = aliasManager.getAliasInfo(expressionString);
            Expression expr;

            if (aliasInfo instanceof SelectInfo) {
                SelectInfo selectInfo = (SelectInfo) aliasInfo;
                expr = selectInfo.getExpression();
                if (clausesRequiredForResultUniqueness != null) {
                    expressionStringBuilder.setLength(0);
                    expr.accept(queryGenerator);
                    clausesRequiredForResultUniqueness.remove(expressionStringBuilder.toString());
                }
            } else {
                expr = orderByInfo.getExpression();
                if (clausesRequiredForResultUniqueness != null) {
                    expressionStringBuilder.setLength(0);
                    expr.accept(queryGenerator);
                    clausesRequiredForResultUniqueness.remove(expressionStringBuilder.toString());
                }
            }

            // Note that we could actually determine a lot more non-nullable cases, but this requires analyzing the query for top level predicates
            // Detecting top-level predicates is out of scope right now and will be done as part of #610
            boolean nullable = ExpressionUtils.isNullable(metamodel, expr);

            // Note that there are actually two notions of uniqueness that we have to check for
            // There is a result uniqueness which is relevant for the safety checks we do
            // and there is a also the general uniqueness which is what is relevant for keyset pagination
            //
            // The general uniqueness can be inferred, when a path expression refers to a unique attribute and parent joins are "uniqueness preserving"
            // A join node is uniqueness preserving when it is a join of a one-to-one or more generally, when there is a top-level equality predicate between unique keys
            // Detecting top-level equality predicates is out of scope right now and will be done as part of #610

            // Normally, when there are multiple query roots, we can only determine uniqueness when query roots are somehow joined by a unique attributes
            // Since that is out of scope now, we require that there must be a single root in order for us to detect uniqueness properly
            boolean unique;
            List<Expression> splitOffExpressions;
            // Determining general uniqueness requires that no collection joins are involved in a query which is kind of guaranteed by design by the PaginatedCriteriaBuilder
            if (joinManager.getRoots().size() == 1 && !hasCollections) {
                unique = functionalDependencyAnalyzerVisitor.analyzeFormsUniqueTuple(expr);
                splitOffExpressions = functionalDependencyAnalyzerVisitor.getSplittedOffExpressions();
            } else {
                unique = false;
                splitOffExpressions = embeddableSplittingVisitor.splitOff(expr);
            }

            resultUnique = resultUnique || unique || clausesRequiredForResultUniqueness != null && clausesRequiredForResultUniqueness.isEmpty();
            boolean resUnique = resultUnique || (i + 1) == size && functionalDependencyAnalyzerVisitor.isResultUnique();
            if (splitOffExpressions.isEmpty()) {
                realExpressions.add(new OrderByExpression(orderByInfo.ascending, orderByInfo.nullFirst, expr, nullable, unique, resUnique));
            } else {
                for (Expression splitOffExpression : splitOffExpressions) {
                    realExpressions.add(new OrderByExpression(orderByInfo.ascending, orderByInfo.nullFirst, splitOffExpression, nullable, unique, resUnique));
                }
            }
        }
        queryGenerator.setQueryBuffer(null);

        return realExpressions;
    }

    boolean hasOrderBys() {
        return orderByInfos.size() > 0;
    }

    boolean hasComplexOrderBys() {
        if (orderByInfos.isEmpty()) {
            return false;
        }

        List<OrderByInfo> infos = orderByInfos;
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            final OrderByInfo orderByInfo = infos.get(i);
            AliasInfo aliasInfo = aliasManager.getAliasInfo(orderByInfo.getExpressionString());
            if (aliasInfo instanceof SelectInfo) {
                SelectInfo selectInfo = (SelectInfo) aliasInfo;
                if (!(selectInfo.getExpression() instanceof PathExpression)) {
                    return true;
                }
            }
            // illegal no path expressions are prevented by the parser
        }

        return false;
    }

    void orderBy(Expression expr, boolean ascending, boolean nullFirst) {
        orderByInfos.add(new OrderByInfo(expr, ascending, nullFirst));
        registerParameterExpressions(expr);
    }

    void acceptVisitor(Expression.Visitor v) {
        List<OrderByInfo> infos = orderByInfos;
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            final OrderByInfo orderByInfo = infos.get(i);
            orderByInfo.getExpression().accept(v);
        }
    }

    <X> X acceptVisitor(Expression.ResultVisitor<X> v, X stopValue) {
        List<OrderByInfo> infos = orderByInfos;
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            final OrderByInfo orderByInfo = infos.get(i);
            if (stopValue.equals(orderByInfo.getExpression().accept(v))) {
                return stopValue;
            }
        }

        return null;
    }

    @Override
    public void apply(ExpressionModifierVisitor<? super ExpressionModifier> visitor) {
        List<OrderByInfo> infos = orderByInfos;
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            final OrderByInfo orderByInfo = infos.get(i);
            visitor.visit(orderByInfo, ClauseType.ORDER_BY);
        }
    }

    void buildSelectClauses(StringBuilder sb, boolean allClauses, int[] keysetToSelectIndexMapping) {
        if (orderByInfos.isEmpty()) {
            return;
        }

        queryGenerator.setClauseType(ClauseType.SELECT);
        queryGenerator.setQueryBuffer(sb);
        SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.CASE_WHEN);

        List<OrderByInfo> infos = orderByInfos;
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            if (keysetToSelectIndexMapping != null && keysetToSelectIndexMapping[i] != -1) {
                continue;
            }
            final OrderByInfo orderByInfo = infos.get(i);
            String potentialSelectAlias = orderByInfo.getExpressionString();
            AliasInfo aliasInfo = aliasManager.getAliasInfo(potentialSelectAlias);
            if (aliasInfo instanceof SelectInfo) {
                SelectInfo selectInfo = (SelectInfo) aliasInfo;

                if (allClauses || !(selectInfo.getExpression() instanceof PathExpression)) {
                    sb.append(", ");
                    queryGenerator.generate(selectInfo.getExpression());
                    sb.append(" AS ").append(potentialSelectAlias);
                }
            } else if (allClauses) {
                sb.append(", ");
                queryGenerator.generate(orderByInfo.getExpression());
            }
        }

        queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
        queryGenerator.setClauseType(null);
    }

    /**
     * Builds the clauses needed for the group by clause for a query that uses aggregate functions to work.
     * 
     * @return
     */
    void buildGroupByClauses(GroupByManager groupByManager, boolean hasGroupBy) {
        if (orderByInfos.isEmpty()) {
            return;
        }

        SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.CASE_WHEN);
        StringBuilder sb = new StringBuilder();

        List<OrderByInfo> infos = orderByInfos;
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            final OrderByInfo orderByInfo = infos.get(i);
            
            String potentialSelectAlias = orderByInfo.getExpressionString();
            AliasInfo aliasInfo = aliasManager.getAliasInfo(potentialSelectAlias);
            Expression expr;

            if (aliasInfo instanceof SelectInfo) {
                SelectInfo selectInfo = (SelectInfo) aliasInfo;
                expr = selectInfo.getExpression();
            } else {
                expr = orderByInfo.getExpression();
            }

            Set<Expression> extractedGroupByExpressions = groupByExpressionGatheringVisitor.extractGroupByExpressions(expr);
            if (!extractedGroupByExpressions.isEmpty()) {
                queryGenerator.setClauseType(ClauseType.GROUP_BY);
                queryGenerator.setQueryBuffer(sb);
                for (Expression expression : extractedGroupByExpressions) {
                    sb.setLength(0);
                    queryGenerator.generate(expression);
                    if (jpaProvider.supportsNullPrecedenceExpression()) {
                        groupByManager.collect(new ResolvedExpression(sb.toString(), expression), ClauseType.ORDER_BY, hasGroupBy);
                    } else {
                        String expressionString = sb.toString();
                        sb.setLength(0);
                        jpaProvider.renderNullPrecedence(sb, expressionString, expressionString, null, null);

                        groupByManager.collect(new ResolvedExpression(sb.toString(), expression), ClauseType.ORDER_BY, hasGroupBy);
                    }
                }
                queryGenerator.setClauseType(null);
            }
        }

        queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
        groupByExpressionGatheringVisitor.clear();
    }

    void buildOrderBy(StringBuilder sb, boolean inverseOrder, boolean resolveSelectAliases, boolean resolveSimpleSelectAliases) {
        if (orderByInfos.isEmpty()) {
            return;
        }
        queryGenerator.setClauseType(ClauseType.ORDER_BY);
        queryGenerator.setQueryBuffer(sb);
        boolean originalResolveSelectAliases = queryGenerator.isResolveSelectAliases();
        queryGenerator.setResolveSelectAliases(resolveSelectAliases);
        sb.append(" ORDER BY ");

        SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.CASE_WHEN);

        List<OrderByInfo> infos = orderByInfos;
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            
            applyOrderBy(sb, infos.get(i), inverseOrder, resolveSimpleSelectAliases);
        }
        queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
        queryGenerator.setClauseType(null);
        queryGenerator.setResolveSelectAliases(originalResolveSelectAliases);
    }

    private void applyOrderBy(StringBuilder sb, OrderByInfo orderBy, boolean inverseOrder, boolean resolveSimpleSelectAliases) {
        AliasInfo aliasInfo = aliasManager.getAliasInfo(orderBy.getExpressionString());
        if (jpaProvider.supportsNullPrecedenceExpression()) {
            queryGenerator.setClauseType(ClauseType.ORDER_BY);
            queryGenerator.setQueryBuffer(sb);
            boolean nullable;
            if (aliasInfo instanceof SelectInfo) {
                Expression selectExpression = ((SelectInfo) aliasInfo).getExpression();
                if (resolveSimpleSelectAliases && selectExpression instanceof PathExpression) {
                    queryGenerator.generate(selectExpression);
                } else {
                    queryGenerator.generate(orderBy.getExpression());
                }
                nullable = ExpressionUtils.isNullable(metamodel, selectExpression);
            } else {
                queryGenerator.generate(orderBy.getExpression());
                nullable = ExpressionUtils.isNullable(metamodel, orderBy.getExpression());
            }

            if (orderBy.ascending == inverseOrder) {
                sb.append(" DESC");
            } else {
                sb.append(" ASC");
            }
            // If the expression isn't nullable, we don't need the nulls clause
            // This is important for databases that don't support the clause and need emulation
            if (nullable) {
                if (orderBy.nullFirst == inverseOrder) {
                    sb.append(" NULLS LAST");
                } else {
                    sb.append(" NULLS FIRST");
                }
            }
        } else {
            String expression;
            String resolvedExpression;
            String order;
            String nulls;
            StringBuilder expressionSb = new StringBuilder();
            Expression orderExpression = orderBy.getExpression();

            queryGenerator.setClauseType(ClauseType.ORDER_BY);
            queryGenerator.setQueryBuffer(expressionSb);

            boolean nullable;
            if (aliasInfo instanceof SelectInfo) {
                Expression selectExpression = ((SelectInfo) aliasInfo).getExpression();
                queryGenerator.generate(selectExpression);
                resolvedExpression = expressionSb.toString();
                if (resolveSimpleSelectAliases && selectExpression instanceof PathExpression) {
                    orderExpression = selectExpression;
                }
                nullable = ExpressionUtils.isNullable(metamodel, selectExpression);
            } else {
                resolvedExpression = null;
                nullable = ExpressionUtils.isNullable(metamodel, orderExpression);
            }

            if (queryGenerator.isResolveSelectAliases() && resolvedExpression != null) {
                expression = resolvedExpression;
            } else {
                expressionSb.setLength(0);
                queryGenerator.generate(orderExpression);
                expression = expressionSb.toString();
            }

            if (orderBy.ascending == inverseOrder) {
                order = "DESC";
            } else {
                order = "ASC";
            }
            if (nullable) {
                if (orderBy.nullFirst == inverseOrder) {
                    nulls = "LAST";
                } else {
                    nulls = "FIRST";
                }
            } else {
                // If the expression isn't nullable, we don't need the nulls clause
                // This is important for databases that don't support the clause and need emulation
                nulls = null;
            }

            jpaProvider.renderNullPrecedence(sb, expression, resolvedExpression, order, nulls);
        }
        queryGenerator.setClauseType(null);
    }

    // TODO: needs equals-hashCode implementation

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static class OrderByInfo extends NodeInfo {

        private String expressionString;
        private boolean ascending;
        private boolean nullFirst;

        public OrderByInfo(Expression expression, boolean ascending, boolean nullFirst) {
            super(expression);
            this.expressionString = expression.toString();
            this.ascending = ascending;
            this.nullFirst = nullFirst;
        }

        public String getExpressionString() {
            return expressionString;
        }

        @Override
        public void setExpression(Expression expression) {
            super.setExpression(expression);
            this.expressionString = expression.toString();
        }

        @Override
        public void set(Expression expression) {
            super.set(expression);
            this.expressionString = expression.toString();
        }

        @Override
        public OrderByInfo clone() {
            return new OrderByInfo(getExpression(), ascending, nullFirst);
        }
    }
}
