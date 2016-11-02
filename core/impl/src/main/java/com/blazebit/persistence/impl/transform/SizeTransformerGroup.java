/*
 * Copyright 2014 - 2016 Blazebit.
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

package com.blazebit.persistence.impl.transform;

import com.blazebit.persistence.impl.AbstractManager;
import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.GroupByManager;
import com.blazebit.persistence.impl.JoinManager;
import com.blazebit.persistence.impl.JoinNode;
import com.blazebit.persistence.impl.OrderByManager;
import com.blazebit.persistence.impl.SelectManager;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PathReference;
import com.blazebit.persistence.impl.expression.SimplePathReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 27.09.2016.
 */
public class SizeTransformerGroup implements ExpressionTransformerGroup {

    private final Map<ClauseType, Object> transformers = new HashMap<ClauseType, Object>();
    private final SizeTransformationVisitor sizeTransformationVisitor;
    private final SelectManager<?> selectManager;
    private final JoinManager joinManager;
    private final GroupByManager groupByManager;

    public SizeTransformerGroup(SizeTransformationVisitor sizeTransformationVisitor, OrderByManager orderByManager, SelectManager<?> selectManager, JoinManager joinManager, GroupByManager groupByManager) {
        this.sizeTransformationVisitor = sizeTransformationVisitor;
        this.selectManager = selectManager;
        this.joinManager = joinManager;
        this.groupByManager = groupByManager;

        SizeExpressionTransformer sizeExpressionTransformer = new SizeExpressionTransformer(sizeTransformationVisitor, selectManager);
        SizeSelectInfoTransformer sizeSelectExpressionTransformer = new SizeSelectInfoTransformer(sizeTransformationVisitor, orderByManager, selectManager);

        transformers.put(ClauseType.WHERE, sizeExpressionTransformer);
        transformers.put(ClauseType.JOIN, sizeExpressionTransformer);
        transformers.put(ClauseType.GROUP_BY, sizeExpressionTransformer);
        transformers.put(ClauseType.HAVING, sizeExpressionTransformer);
        transformers.put(ClauseType.ORDER_BY, sizeExpressionTransformer);
        transformers.put(ClauseType.SELECT, sizeSelectExpressionTransformer);
    }

    @Override
    public void applyExpressionTransformer(AbstractManager manager) {
        if (manager.getClauseType() != ClauseType.SELECT || selectManager.containsSizeSelect()) {
            Object transformer = transformers.get(manager.getClauseType());
            if (transformer != null) {
                if (transformer instanceof ExpressionTransformer) {
                    manager.applyTransformer((ExpressionTransformer) transformer);
                } else if (transformer instanceof SelectInfoTransformer) {
                    if (manager instanceof SelectManager) {
                        ((SelectManager<?>) manager).applySelectInfoTransformer((SelectInfoTransformer) transformer);
                    } else {
                        throw new RuntimeException("Manager type [" + manager.getClass().getName() + "] does not accept transformer of type [" + transformer.getClass().getName() + "]");
                    }
                } else {
                    throw new RuntimeException("Unsupported transformer type [" + transformer.getClass().getName() + "]");
                }
            }
        }
    }

    @Override
    public void afterGlobalTransformation() {
        // finally add the required joins for the transformations that were carried out
        for (SizeTransformationVisitor.LateJoinEntry lateJoinEntry : sizeTransformationVisitor.getLateJoins().values()) {
            PathExpression requiredJoinExpression = lateJoinEntry.getPathsToJoin().get(0);
            joinManager.implicitJoin(requiredJoinExpression, true, null, null, false, false, true);
            PathReference generatedJoin = requiredJoinExpression.getPathReference();
            ((JoinNode) generatedJoin.getBaseNode()).getClauseDependencies().addAll(lateJoinEntry.getClauseDependencies());
            for (int i = 1; i < lateJoinEntry.getPathsToJoin().size(); i++) {
                lateJoinEntry.getPathsToJoin().get(i).setPathReference(new SimplePathReference(generatedJoin.getBaseNode(), generatedJoin.getField(), null));
            }
        }

        for (PathExpression groupByExpr : sizeTransformationVisitor.getRequiredGroupBys()) {
            groupByManager.groupBy(groupByExpr);
        }
        if (groupByManager.hasGroupBys()) {
            for (PathExpression groupByExpr : sizeTransformationVisitor.getRequiredGroupBysIfOtherGroupBys()) {
                groupByManager.groupBy(groupByExpr);
            }
        }
    }
}
