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

import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.FooExpression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.predicate.VisitorAdapter;
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

    public SizeSelectToCountTransformer(JoinManager joinManager, GroupByManager groupByManager, OrderByManager orderByManager) {
        this.joinManager = joinManager;
        this.groupByManager = groupByManager;
        this.orderByManager = orderByManager;
    }

    @Override
    public void transform(SelectInfo info) {
        if (ExpressionUtils.isSizeExpression(info.getExpression())) {
            PathExpression sizeArg = (PathExpression) ((CompositeExpression) info.getExpression()).getExpressions().get(1);
            CompositeExpression countExpr = new CompositeExpression(new ArrayList<Expression>());
            countExpr.getExpressions().add(new FooExpression("COUNT("));
            sizeArg.setUsedInCollectionFunction(false);
            countExpr.getExpressions().add(sizeArg);
            countExpr.getExpressions().add(new FooExpression(")"));

            // fromSelect must be false otherwise the join is not rendered in id query since selectOnly would be true
            joinManager.implicitJoin(sizeArg, true, ClauseType.SELECT, false, false);

            info.setExpression(countExpr);
            
            // build group by id clause
            List<PathElementExpression> pathElementExpr = new ArrayList<PathElementExpression>();
            pathElementExpr.add(new PropertyExpression(joinManager.getRootAlias()));
            pathElementExpr.add(new PropertyExpression(joinManager.getRootId()));
            groupByManager.getGroupByInfos().add(new NodeInfo(new PathExpression(pathElementExpr)));
        }

        if (orderByManager.getOrderBySelectAliases().contains(info.getAlias())) {
            info.getExpression().accept(new VisitorAdapter() {

                @Override
                public void visit(PathExpression expression) {
                    ((JoinNode) expression.getBaseNode()).getClauseDependencies().add(ClauseType.ORDER_BY);
                }

            });

        }
    }
}
