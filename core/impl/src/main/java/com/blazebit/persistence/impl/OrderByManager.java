/*
 * Copyright 2014 Blazebit.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.metamodel.Metamodel;

import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.jpaprovider.JpaProvider;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class OrderByManager extends AbstractManager {

    private final List<OrderByInfo> orderByInfos = new ArrayList<OrderByInfo>();
    private final AliasManager aliasManager;
    private final JpaProvider jpaProvider;

    OrderByManager(ResolvingQueryGenerator queryGenerator, ParameterManager parameterManager, AliasManager aliasManager, JpaProvider jpaProvider) {
        super(queryGenerator, parameterManager);
        this.aliasManager = aliasManager;
        this.jpaProvider = jpaProvider;
    }

    Set<String> getOrderBySelectAliases() {
        if (orderByInfos.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> orderBySelectAliases = new HashSet<String>();
        for (OrderByInfo orderByInfo : orderByInfos) {
            String potentialSelectAlias = orderByInfo.getExpression().toString();
            if (aliasManager.isSelectAlias(potentialSelectAlias)) {
                orderBySelectAliases.add(potentialSelectAlias);
            }
        }
        return orderBySelectAliases;
    }

    List<OrderByExpression> getOrderByExpressions(Metamodel metamodel) {
        if (orderByInfos.isEmpty()) {
            return Collections.emptyList();
        }

        List<OrderByExpression> realExpressions = new ArrayList<OrderByExpression>(orderByInfos.size());

        for (OrderByInfo orderByInfo : orderByInfos) {
            AliasInfo aliasInfo = aliasManager.getAliasInfo(orderByInfo.getExpression().toString());
            Expression expr;

            if (aliasInfo != null && aliasInfo instanceof SelectInfo) {
                SelectInfo selectInfo = (SelectInfo) aliasInfo;
                expr = selectInfo.getExpression();
            } else {
                expr = orderByInfo.getExpression();
            }

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

        for (OrderByInfo orderByInfo : orderByInfos) {
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
        for (OrderByInfo orderBy : orderByInfos) {
            orderBy.getExpression().accept(v);
        }
    }

    <X> X acceptVisitor(Expression.ResultVisitor<X> v, X stopValue) {
        for (OrderByInfo orderBy : orderByInfos) {
            if (stopValue.equals(orderBy.getExpression().accept(v))) {
                return stopValue;
            }
        }

        return null;
    }

    void applyTransformer(ExpressionTransformer transformer) {
        for (OrderByInfo orderBy : orderByInfos) {
            orderBy.setExpression(transformer.transform(orderBy.getExpression(), ClauseType.ORDER_BY, true));
        }
    }

    void buildSelectClauses(StringBuilder sb, boolean allClauses) {
        if (orderByInfos.isEmpty()) {
            return;
        }

        queryGenerator.setQueryBuffer(sb);
        boolean conditionalContext = queryGenerator.setConditionalContext(false);
        Iterator<OrderByInfo> iter = orderByInfos.iterator();
        OrderByInfo orderByInfo;

        while (iter.hasNext()) {
            orderByInfo = iter.next();
            String potentialSelectAlias = orderByInfo.getExpression().toString();
            AliasInfo aliasInfo = aliasManager.getAliasInfo(potentialSelectAlias);
            if (aliasInfo != null && aliasInfo instanceof SelectInfo) {
                SelectInfo selectInfo = (SelectInfo) aliasInfo;

                if (allClauses || !(selectInfo.getExpression() instanceof PathExpression)) {
                    sb.append(", ");
                    selectInfo.getExpression().accept(queryGenerator);
                    sb.append(" AS ").append(potentialSelectAlias);
                }
            } else if (allClauses) {
                sb.append(", ");
                orderByInfo.getExpression().accept(queryGenerator);
            }
        }

        queryGenerator.setConditionalContext(conditionalContext);
    }

    /**
     * Builds the clauses needed for the group by clause for a query that uses aggregate functions to work.
     * 
     * @return
     */
    Set<String> buildGroupByClauses() {
        if (orderByInfos.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> groupByClauses = new LinkedHashSet<String>();
        Iterator<OrderByInfo> iter = orderByInfos.iterator();
        boolean conditionalContext = queryGenerator.setConditionalContext(false);
        OrderByInfo orderByInfo;
        StringBuilder sb = new StringBuilder();

        while (iter.hasNext()) {
            sb.setLength(0);
            queryGenerator.setQueryBuffer(sb);
            orderByInfo = iter.next();
            String potentialSelectAlias = orderByInfo.getExpression().toString();
            AliasInfo aliasInfo = aliasManager.getAliasInfo(potentialSelectAlias);
            Expression expr;

            if (aliasInfo != null && aliasInfo instanceof SelectInfo) {
                SelectInfo selectInfo = (SelectInfo) aliasInfo;
                expr = selectInfo.getExpression();
            } else {
                expr = orderByInfo.getExpression();
            }

            // This visitor checks if an expression is usable in a group by
            GroupByUsableDetectionVisitor groupByUsableDetectionVisitor = new GroupByUsableDetectionVisitor();
            if (!Boolean.TRUE.equals(expr.accept(groupByUsableDetectionVisitor))) {
                sb.setLength(0);
                queryGenerator.setQueryBuffer(sb);
                expr.accept(queryGenerator);
                groupByClauses.add(sb.toString());
            }
        }

        queryGenerator.setConditionalContext(conditionalContext);
        return groupByClauses;
    }

    void buildOrderBy(StringBuilder sb, boolean inverseOrder, boolean resolveSelectAliases) {
        if (orderByInfos.isEmpty()) {
            return;
        }
        queryGenerator.setQueryBuffer(sb);
        sb.append(" ORDER BY ");
        Iterator<OrderByInfo> iter = orderByInfos.iterator();
        boolean conditionalContext = queryGenerator.setConditionalContext(false);
        applyOrderBy(sb, iter.next(), inverseOrder, resolveSelectAliases);
        while (iter.hasNext()) {
            sb.append(", ");
            applyOrderBy(sb, iter.next(), inverseOrder, resolveSelectAliases);
        }
        queryGenerator.setConditionalContext(conditionalContext);
    }

    private void applyOrderBy(StringBuilder sb, OrderByInfo orderBy, boolean inverseOrder, boolean resolveSelectAliases) {
        if (jpaProvider.supportsNullPrecedenceExpression()) {
            if (resolveSelectAliases) {
                AliasInfo aliasInfo = aliasManager.getAliasInfo(orderBy.getExpression().toString());
                // NOTE: Originally we restricted this to path expressions, but since I don't know the reason for that anymore, we
                // removed it
                if (aliasInfo != null && aliasInfo instanceof SelectInfo) {
                    ((SelectInfo) aliasInfo).getExpression().accept(queryGenerator);
                } else {
                    orderBy.getExpression().accept(queryGenerator);
                }
            } else {
                orderBy.getExpression().accept(queryGenerator);
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

            queryGenerator.setQueryBuffer(expressionSb);

            AliasInfo aliasInfo = aliasManager.getAliasInfo(orderBy.getExpression().toString());
            // NOTE: Originally we restricted this to path expressions, but since I don't know the reason for that anymore, we removed
            // it
            if (aliasInfo != null && aliasInfo instanceof SelectInfo) {
                ((SelectInfo) aliasInfo).getExpression().accept(queryGenerator);
                resolvedExpression = expressionSb.toString();
            } else {
                resolvedExpression = null;
            }

            if (resolveSelectAliases && resolvedExpression != null) {
                expression = resolvedExpression;
            } else {
                expressionSb.setLength(0);
                orderBy.getExpression().accept(queryGenerator);
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

            sb.append(jpaProvider.renderNullPrecedence(expression, resolvedExpression, order, nulls));
        }
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
    }
}
