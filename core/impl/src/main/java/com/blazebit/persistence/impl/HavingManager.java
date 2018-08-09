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

import java.util.Set;

import com.blazebit.persistence.impl.builder.predicate.HavingOrBuilderImpl;
import com.blazebit.persistence.parser.SimpleQueryGenerator;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class HavingManager<T> extends PredicateManager<T> {

    private final GroupByExpressionGatheringVisitor groupByExpressionGatheringVisitor;

    HavingManager(ResolvingQueryGenerator queryGenerator, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, GroupByExpressionGatheringVisitor groupByExpressionGatheringVisitor) {
        super(queryGenerator, parameterManager, subqueryInitFactory, expressionFactory);
        this.groupByExpressionGatheringVisitor = groupByExpressionGatheringVisitor;
    }

    @Override
    protected String getClauseName() {
        return "HAVING";
    }

    @Override
    public ClauseType getClauseType() {
        return ClauseType.HAVING;
    }

    @SuppressWarnings("unchecked")
    HavingOrBuilderImpl<T> havingOr(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> builder) {
        return rootPredicate.startBuilder(new HavingOrBuilderImpl<T>((T) builder, rootPredicate, subqueryInitFactory, expressionFactory, parameterManager));
    }

    void buildGroupByClauses(GroupByManager groupByManager, boolean hasGroupBy) {
        if (rootPredicate.getPredicate().getChildren().isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        queryGenerator.setQueryBuffer(sb);
        queryGenerator.setClauseType(ClauseType.GROUP_BY);
        SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.PREDICATE);

        Set<Expression> extractedGroupByExpressions = groupByExpressionGatheringVisitor.extractGroupByExpressions(rootPredicate.getPredicate());
        if (!extractedGroupByExpressions.isEmpty()) {
            for (Expression expr : extractedGroupByExpressions) {
                queryGenerator.generate(expr);
                groupByManager.collect(new ResolvedExpression(sb.toString(), expr), ClauseType.HAVING, hasGroupBy);
                sb.setLength(0);
            }
        }
        
        queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
        queryGenerator.setClauseType(null);
        groupByExpressionGatheringVisitor.clear();
    }

    public boolean isEmpty() {
        return rootPredicate.getPredicate().getChildren().isEmpty();
    }
}
