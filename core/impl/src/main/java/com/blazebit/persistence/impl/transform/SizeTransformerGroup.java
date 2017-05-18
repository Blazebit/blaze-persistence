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

package com.blazebit.persistence.impl.transform;

import com.blazebit.persistence.impl.AbstractManager;
import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.GroupByManager;
import com.blazebit.persistence.impl.JoinManager;
import com.blazebit.persistence.impl.OrderByManager;
import com.blazebit.persistence.impl.ResolvedExpression;
import com.blazebit.persistence.impl.SelectInfo;
import com.blazebit.persistence.impl.SelectManager;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.modifier.ExpressionModifier;

import java.util.*;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public class SizeTransformerGroup implements ExpressionTransformerGroup<ExpressionModifier> {

    private final SizeTransformationVisitor sizeTransformationVisitor;
    private final SelectManager<?> selectManager;
    private final JoinManager joinManager;
    private final GroupByManager groupByManager;
    private final SizeExpressionTransformer sizeExpressionTransformer;
    private final SizeSelectInfoTransformer sizeSelectExpressionTransformer;

    public SizeTransformerGroup(SizeTransformationVisitor sizeTransformationVisitor, OrderByManager orderByManager, SelectManager<?> selectManager, JoinManager joinManager, GroupByManager groupByManager) {
        this.sizeTransformationVisitor = sizeTransformationVisitor;
        this.selectManager = selectManager;
        this.joinManager = joinManager;
        this.sizeExpressionTransformer = new SizeExpressionTransformer(sizeTransformationVisitor);
        this.groupByManager = groupByManager;
        this.sizeSelectExpressionTransformer = new SizeSelectInfoTransformer(sizeTransformationVisitor, orderByManager);
    }

    @Override
    public void applyExpressionTransformer(AbstractManager<? extends ExpressionModifier> manager) {
        if (manager.getClauseType() != ClauseType.SELECT || selectManager.containsSizeSelect()) {
            switch (manager.getClauseType()) {
                case WHERE:
                case JOIN:
                case GROUP_BY:
                case HAVING:
                case ORDER_BY:
                    manager.apply(sizeExpressionTransformer);
                    break;
                case SELECT:
                    ((AbstractManager<SelectInfo>) manager).apply(sizeSelectExpressionTransformer);
                    break;
                default:
                    // Ignore
            }
        }
    }

    @Override
    public void afterTransformationGroup() {
        // finally add the required joins for the transformations that were carried out
        for (SizeTransformationVisitor.LateJoinEntry lateJoinEntry : sizeTransformationVisitor.getLateJoins().values()) {
            for (Expression requiredJoinExpression : lateJoinEntry.getExpressionsToJoin()) {
                for (ClauseType clauseType : lateJoinEntry.getClauseDependencies()) {
                    joinManager.implicitJoin(requiredJoinExpression, true, null, clauseType, null, false, false, true, false);
                }
            }
        }

        for (Map.Entry<ResolvedExpression, Set<ClauseType>> entry : sizeTransformationVisitor.getRequiredGroupBys().entrySet()) {
            groupByManager.collect(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void afterAllTransformations() {
        if (groupByManager.hasCollectedGroupByClauses()) {
            for (Map.Entry<ResolvedExpression, Set<ClauseType>> entry : sizeTransformationVisitor.getSubqueryGroupBys().entrySet()) {
                groupByManager.collect(entry.getKey(), entry.getValue());
            }
        }
    }
}
