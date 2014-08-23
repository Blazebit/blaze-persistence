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

import com.blazebit.persistence.impl.expression.Expression;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class OrderByManager extends AbstractManager {

    private final List<OrderByInfo> orderByInfos = new ArrayList<OrderByInfo>();
    private final AliasManager aliasManager;
    private final String fromClassName;

    OrderByManager(QueryGenerator queryGenerator, ParameterManager parameterManager, AliasManager aliasManager, String fromClassName) {
        super(queryGenerator, parameterManager);
        this.aliasManager = aliasManager;
        this.fromClassName = fromClassName;
    }

    List<Expression> getRealExpressions() {
        if (orderByInfos.isEmpty()) {
            return Collections.emptyList();
        }

        List<Expression> realExpressions = new ArrayList<Expression>(orderByInfos.size());

        for (OrderByInfo orderByInfo : orderByInfos) {
            AliasInfo aliasInfo = aliasManager.getAliasInfo(orderByInfo.getExpression().toString());
            if (aliasInfo != null && aliasInfo instanceof SelectInfo) {
                SelectInfo selectInfo = (SelectInfo) aliasInfo;
                realExpressions.add(selectInfo.getExpression());
            } else {
                realExpressions.add(orderByInfo.getExpression());
            }
        }

        return realExpressions;
    }

    String[] getAbsoluteExpressionStrings() {
        if (orderByInfos.isEmpty()) {
            return new String[0];
        }

        String[] absoluteExpressionStrings = new String[orderByInfos.size()];
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < orderByInfos.size(); i++) {
            OrderByInfo orderByInfo = orderByInfos.get(i);
            AliasInfo aliasInfo = aliasManager.getAliasInfo(orderByInfo.getExpression().toString());
            sb.delete(0, sb.length());

            if (aliasInfo != null && aliasInfo instanceof SelectInfo) {
                SelectInfo selectInfo = (SelectInfo) aliasInfo;
                selectInfo.getExpression().accept(new AbsoluteExpressionStringVisitor(sb, fromClassName));
            } else {
                orderByInfo.getExpression().accept(new AbsoluteExpressionStringVisitor(sb, fromClassName));
            }

            if (orderByInfo.ascending) {
                sb.append(" ASC");
            } else {
                sb.append(" DESC");
            }

            if (orderByInfo.nullFirst) {
                sb.append(" NULLS FIRST");
            } else {
                sb.append(" NULLS LAST");
            }

            absoluteExpressionStrings[i] = sb.toString();
        }

        return absoluteExpressionStrings;
    }

    boolean hasOrderBys() {
        return orderByInfos.size() > 0;
    }

    boolean hasOrderBys(boolean allClauses) {
        if (orderByInfos.size() > 0) {
            if (allClauses) {
                return true;
            }
        } else {
            return false;
        }

        for (OrderByInfo orderByInfo : orderByInfos) {
            AliasInfo aliasInfo = aliasManager.getAliasInfo(orderByInfo.getExpression().toString());
            if (aliasInfo != null && aliasInfo instanceof SelectInfo) {
                SelectInfo selectInfo = (SelectInfo) aliasInfo;
                if (ExpressionUtils.containsSubqueryExpression(selectInfo.getExpression())) {
                    return true;
                }
            }
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

    void applyTransformer(ExpressionTransformer transformer) {
        for (OrderByInfo orderBy : orderByInfos) {
            orderBy.setExpression(transformer.transform(orderBy.getExpression()));
        }
    }

    void buildSelectClauses(StringBuilder sb, boolean allClauses) {
        if (orderByInfos.isEmpty()) {
            return;
        }

        queryGenerator.setQueryBuffer(sb);
        Iterator<OrderByInfo> iter = orderByInfos.iterator();
        OrderByInfo orderByInfo;

        orderByInfo = iter.next();
        String potentialSelectAlias = orderByInfo.getExpression().toString();
        AliasInfo aliasInfo = aliasManager.getAliasInfo(potentialSelectAlias);
        if (aliasInfo != null && aliasInfo instanceof SelectInfo) {
            SelectInfo selectInfo = (SelectInfo) aliasInfo;
            if (allClauses || ExpressionUtils.containsSubqueryExpression(selectInfo.getExpression())) {
                selectInfo.getExpression().accept(queryGenerator);
                sb.append(" AS ").append(potentialSelectAlias);
            }
        } else if (allClauses) {
            orderByInfo.getExpression().accept(queryGenerator);
        }

        while (iter.hasNext()) {
            orderByInfo = iter.next();
            potentialSelectAlias = orderByInfo.getExpression().toString();
            aliasInfo = aliasManager.getAliasInfo(potentialSelectAlias);
            if (aliasInfo != null && aliasInfo instanceof SelectInfo) {
                SelectInfo selectInfo = (SelectInfo) aliasInfo;
                if (allClauses || ExpressionUtils.containsSubqueryExpression(selectInfo.getExpression())) {
                    sb.append(", ");
                    selectInfo.getExpression().accept(queryGenerator);
                    sb.append(" AS ").append(potentialSelectAlias);
                }
            } else if (allClauses) {
                sb.append(", ");
                orderByInfo.getExpression().accept(queryGenerator);
            }
        }
    }

    void buildOrderBy(StringBuilder sb) {
        if (orderByInfos.isEmpty()) {
            return;
        }
        queryGenerator.setQueryBuffer(sb);
        sb.append(" ORDER BY ");
        Iterator<OrderByInfo> iter = orderByInfos.iterator();
        applyOrderBy(sb, iter.next());
        while (iter.hasNext()) {
            sb.append(", ");
            applyOrderBy(sb, iter.next());
        }
    }

    private void applyOrderBy(StringBuilder sb, OrderByInfo orderBy) {
        orderBy.getExpression().accept(queryGenerator);
        if (!orderBy.ascending) {
            sb.append(" DESC");
        } else {
            sb.append(" ASC");
        }
        if (orderBy.nullFirst) {
            sb.append(" NULLS FIRST");
        } else {
            sb.append(" NULLS LAST");
        }
    }

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
