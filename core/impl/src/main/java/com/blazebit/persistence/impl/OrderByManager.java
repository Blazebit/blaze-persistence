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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.metamodel.Metamodel;

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

    Set<String> getOrderBySelectAliases(){
        if (orderByInfos.isEmpty()) {
            return Collections.emptySet();
        }
        
        Set<String> orderBySelectAliases = new HashSet<String>();
        for(OrderByInfo orderByInfo : orderByInfos){
            String potentialSelectAlias = orderByInfo.getExpression().toString();
            if(aliasManager.isSelectAlias(potentialSelectAlias)){
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

    boolean hasSubqueryOrderBys() {
        if (orderByInfos.isEmpty()) {
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
            orderBy.setExpression(transformer.transform(orderBy.getExpression(), ClauseType.ORDER_BY));
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

    void buildOrderBy(StringBuilder sb, boolean inverseOrder, boolean resolveSelectAliases) {
        if (orderByInfos.isEmpty()) {
            return;
        }
        queryGenerator.setQueryBuffer(sb);
        sb.append(" ORDER BY ");
        Iterator<OrderByInfo> iter = orderByInfos.iterator();
        applyOrderBy(sb, iter.next(), inverseOrder, resolveSelectAliases);
        while (iter.hasNext()) {
            sb.append(", ");
            applyOrderBy(sb, iter.next(), inverseOrder, resolveSelectAliases);
        }
    }

    private void applyOrderBy(StringBuilder sb, OrderByInfo orderBy, boolean inverseOrder, boolean resolveSelectAliases) {
        if(resolveSelectAliases){
            AliasInfo aliasInfo = aliasManager.getAliasInfo(orderBy.getExpression().toString());
            if(aliasInfo != null && aliasInfo instanceof SelectInfo && !ExpressionUtils.containsSubqueryExpression(((SelectInfo)aliasInfo).getExpression())){
                ((SelectInfo)aliasInfo).getExpression().accept(queryGenerator);
            }else{
                orderBy.getExpression().accept(queryGenerator);
            }
        }else{
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
