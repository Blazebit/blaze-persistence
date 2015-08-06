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

import com.blazebit.persistence.impl.expression.AggregateExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Moritz Becker
 */
public class SizeSelectToCountTransformer implements SelectInfoTransformer {

    private final JoinManager joinManager;
    private final GroupByManager groupByManager;
    private final OrderByManager orderByManager;
    private final DeepSizeSelectToCountTransformer deepTransformer = new DeepSizeSelectToCountTransformer();

    public SizeSelectToCountTransformer(JoinManager joinManager, GroupByManager groupByManager, OrderByManager orderByManager) {
        this.joinManager = joinManager;
        this.groupByManager = groupByManager;
        this.orderByManager = orderByManager;
    }

    @Override
    public void transform(SelectInfo info) {
        deepTransformer.setOrderBySelectClause(orderByManager.getOrderBySelectAliases().contains(info.getAlias()));
        if (ExpressionUtils.isSizeFunction(info.getExpression())) {
            info.setExpression(info.getExpression().accept(deepTransformer));
        } else {
            info.getExpression().accept(deepTransformer);

        }
    }

    private class DeepSizeSelectToCountTransformer extends SizeTransformationVisitor {

        private boolean orderBySelectClause;

        public void setOrderBySelectClause(boolean orderBySelectClause) {
            this.orderBySelectClause = orderBySelectClause;
        }

        @Override
        public Expression visit(PathExpression expression) {
            if (orderBySelectClause) {
                ((JoinNode) expression.getBaseNode()).getClauseDependencies().add(ClauseType.ORDER_BY);
            }
            return expression;
        }

        @Override
        public Expression visit(FunctionExpression expression) {
            if (ExpressionUtils.isSizeFunction(expression)) {
                PathExpression sizeArg = (PathExpression) expression.getExpressions().get(0);
                sizeArg.setUsedInCollectionFunction(false);
                AggregateExpression countExpr = new AggregateExpression(false, "COUNT", expression.getExpressions());

                joinManager.implicitJoin(sizeArg, true, ClauseType.SELECT, false, false);

                // build group by id clause
                List<PathElementExpression> pathElementExpr = new ArrayList<PathElementExpression>();
                pathElementExpr.add(new PropertyExpression(joinManager.getRootAlias()));
                pathElementExpr.add(new PropertyExpression(joinManager.getRootId()));
                groupByManager.groupBy(new PathExpression(pathElementExpr));
                super.visit(expression);

                return countExpr;
            }
            return expression;
        }
    }
}
