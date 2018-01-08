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

import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.modifier.ExpressionModifier;
import com.blazebit.persistence.impl.transform.ExpressionModifierVisitor;
import com.blazebit.persistence.spi.JpaProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class OrderByManager extends AbstractManager<ExpressionModifier> {

    private final GroupByExpressionGatheringVisitor groupByExpressionGatheringVisitor;
    private final List<OrderByInfo> orderByInfos = new ArrayList<OrderByInfo>();
    private final AliasManager aliasManager;
    private final JpaProvider jpaProvider;

    OrderByManager(ResolvingQueryGenerator queryGenerator, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory, AliasManager aliasManager, JpaProvider jpaProvider, GroupByExpressionGatheringVisitor groupByExpressionGatheringVisitor) {
        super(queryGenerator, parameterManager, subqueryInitFactory);
        this.groupByExpressionGatheringVisitor = groupByExpressionGatheringVisitor;
        this.aliasManager = aliasManager;
        this.jpaProvider = jpaProvider;
    }

    void applyFrom(OrderByManager orderByManager) {
        for (OrderByInfo info : orderByManager.orderByInfos) {
            orderBy(subqueryInitFactory.reattachSubqueries(info.getExpression().clone(true)), info.ascending, info.nullFirst);
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
            String potentialSelectAlias = orderByInfo.getExpression().toString();
            if (alias.equals(potentialSelectAlias)) {
                return true;
            }
        }
        return false;
    }

    List<OrderByExpression> getOrderByExpressions(EntityMetamodel metamodel) {
        if (orderByInfos.isEmpty()) {
            return Collections.emptyList();
        }

        List<OrderByExpression> realExpressions = new ArrayList<OrderByExpression>(orderByInfos.size());

        List<OrderByInfo> infos = orderByInfos;
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            final OrderByInfo orderByInfo = infos.get(i);
            AliasInfo aliasInfo = aliasManager.getAliasInfo(orderByInfo.getExpression().toString());
            Expression expr;

            if (aliasInfo != null && aliasInfo instanceof SelectInfo) {
                SelectInfo selectInfo = (SelectInfo) aliasInfo;
                expr = selectInfo.getExpression();
            } else {
                expr = orderByInfo.getExpression();
            }

            // TODO: This analysis is seriously broken
            // In order to give correct results, we actually have to analyze the whole query
            boolean nullable = ExpressionUtils.isNullable(metamodel, expr);
            boolean unique = ExpressionUtils.isUnique(metamodel, expr);
            realExpressions.add(new OrderByExpression(orderByInfo.ascending, orderByInfo.nullFirst, expr, nullable, unique));
        }

        return realExpressions;
    }

    boolean hasOrderBys() {
        return orderByInfos.size() > 0;
    }

    int getOrderByCount() {
        return orderByInfos.size();
    }

    boolean hasComplexOrderBys() {
        if (orderByInfos.isEmpty()) {
            return false;
        }

        List<OrderByInfo> infos = orderByInfos;
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            final OrderByInfo orderByInfo = infos.get(i);
            AliasInfo aliasInfo = aliasManager.getAliasInfo(orderByInfo.getExpression().toString());
            if (aliasInfo != null && aliasInfo instanceof SelectInfo) {
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

    void buildSelectClauses(StringBuilder sb, boolean allClauses) {
        if (orderByInfos.isEmpty()) {
            return;
        }

        queryGenerator.setClauseType(ClauseType.SELECT);
        queryGenerator.setQueryBuffer(sb);
        SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.CASE_WHEN);

        List<OrderByInfo> infos = orderByInfos;
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            final OrderByInfo orderByInfo = infos.get(i);
            String potentialSelectAlias = orderByInfo.getExpression().toString();
            AliasInfo aliasInfo = aliasManager.getAliasInfo(potentialSelectAlias);
            if (aliasInfo != null && aliasInfo instanceof SelectInfo) {
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
    void buildGroupByClauses(Set<String> clauses) {
        if (orderByInfos.isEmpty()) {
            return;
        }

        SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.CASE_WHEN);
        StringBuilder sb = new StringBuilder();

        List<OrderByInfo> infos = orderByInfos;
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            final OrderByInfo orderByInfo = infos.get(i);
            
            String potentialSelectAlias = orderByInfo.getExpression().toString();
            AliasInfo aliasInfo = aliasManager.getAliasInfo(potentialSelectAlias);
            Expression expr;

            if (aliasInfo != null && aliasInfo instanceof SelectInfo) {
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
                        clauses.add(sb.toString());
                    } else {
                        String expressionString = sb.toString();
                        sb.setLength(0);
                        jpaProvider.renderNullPrecedence(sb, expressionString, expressionString, null, null);

                        clauses.add(sb.toString());
                    }
                }
                queryGenerator.setClauseType(null);
            }
        }

        queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
        groupByExpressionGatheringVisitor.clear();
    }

    void buildOrderBy(StringBuilder sb, boolean inverseOrder, boolean resolveSelectAliases) {
        if (orderByInfos.isEmpty()) {
            return;
        }
        queryGenerator.setClauseType(ClauseType.ORDER_BY);
        queryGenerator.setQueryBuffer(sb);
        sb.append(" ORDER BY ");

        SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.CASE_WHEN);

        List<OrderByInfo> infos = orderByInfos;
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            
            applyOrderBy(sb, infos.get(i), inverseOrder, resolveSelectAliases);
        }
        queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
        queryGenerator.setClauseType(null);
    }

    private void applyOrderBy(StringBuilder sb, OrderByInfo orderBy, boolean inverseOrder, boolean resolveSelectAliases) {
        if (jpaProvider.supportsNullPrecedenceExpression()) {
            queryGenerator.setClauseType(ClauseType.ORDER_BY);
            queryGenerator.setQueryBuffer(sb);
            if (resolveSelectAliases) {
                AliasInfo aliasInfo = aliasManager.getAliasInfo(orderBy.getExpression().toString());
                // NOTE: Originally we restricted this to path expressions, but since I don't know the reason for that anymore, we
                // removed it
                if (aliasInfo != null && aliasInfo instanceof SelectInfo) {
                    queryGenerator.generate(((SelectInfo) aliasInfo).getExpression());
                } else {
                    queryGenerator.generate(orderBy.getExpression());
                }
            } else {
                queryGenerator.generate(orderBy.getExpression());
            }

            if (orderBy.ascending == inverseOrder) {
                sb.append(" DESC");
            } else {
                sb.append(" ASC");
            }
            if (orderBy.nullFirst == inverseOrder) {
                sb.append(" NULLS LAST");
            } else {
                sb.append(" NULLS FIRST");
            }
        } else {
            String expression;
            String resolvedExpression;
            String order;
            String nulls;
            StringBuilder expressionSb = new StringBuilder();

            queryGenerator.setClauseType(ClauseType.ORDER_BY);
            queryGenerator.setQueryBuffer(expressionSb);

            AliasInfo aliasInfo = aliasManager.getAliasInfo(orderBy.getExpression().toString());
            // NOTE: Originally we restricted this to path expressions, but since I don't know the reason for that anymore, we removed
            // it
            if (aliasInfo != null && aliasInfo instanceof SelectInfo) {
                queryGenerator.generate(((SelectInfo) aliasInfo).getExpression());
                resolvedExpression = expressionSb.toString();
            } else {
                resolvedExpression = null;
            }

            if (resolveSelectAliases && resolvedExpression != null) {
                expression = resolvedExpression;
            } else {
                expressionSb.setLength(0);
                queryGenerator.generate(orderBy.getExpression());
                expression = expressionSb.toString();
            }

            if (orderBy.ascending == inverseOrder) {
                order = "DESC";
            } else {
                order = "ASC";
            }
            if (orderBy.nullFirst == inverseOrder) {
                nulls = "LAST";
            } else {
                nulls = "FIRST";
            }

            jpaProvider.renderNullPrecedence(sb, expression, resolvedExpression, order, nulls);
        }
        queryGenerator.setClauseType(null);
    }

    // TODO: needs equals-hashCode implementation

    private static class OrderByInfo extends NodeInfo {

        private boolean ascending;
        private boolean nullFirst;

        public OrderByInfo(Expression expression, boolean ascending, boolean nullFirst) {
            super(expression);
            this.ascending = ascending;
            this.nullFirst = nullFirst;
        }

        @Override
        public OrderByInfo clone() {
            return new OrderByInfo(getExpression(), ascending, nullFirst);
        }
    }
}
