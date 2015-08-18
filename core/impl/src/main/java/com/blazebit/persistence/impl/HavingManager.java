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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.blazebit.persistence.impl.builder.predicate.HavingOrBuilderImpl;
import com.blazebit.persistence.impl.expression.ExpressionFactory;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class HavingManager<T> extends PredicateManager<T> {

    HavingManager(ResolvingQueryGenerator queryGenerator, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        super(queryGenerator, parameterManager, subqueryInitFactory, expressionFactory);
    }

    @Override
    protected String getClauseName() {
        return "HAVING";
    }

    @Override
    protected ClauseType getClauseType() {
        return ClauseType.HAVING;
    }

    @SuppressWarnings("unchecked")
    HavingOrBuilderImpl<T> havingOr(AbstractBaseQueryBuilder<?, ?> builder) {
        return rootPredicate.startBuilder(new HavingOrBuilderImpl<T>((T) builder, rootPredicate, subqueryInitFactory, expressionFactory));
    }

    Set<String> buildGroupByClauses() {
        if (rootPredicate.getPredicate().getChildren().isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> groupByClauses = new LinkedHashSet<String>();
        // TODO: No idea yet how to actually handle this
        boolean conditionalContext = queryGenerator.isConditionalContext();
        GroupByExpressionGatheringVisitor visitor = new GroupByExpressionGatheringVisitor(groupByClauses, queryGenerator);
        rootPredicate.getPredicate().accept(visitor);
        queryGenerator.setConditionalContext(conditionalContext);

        return groupByClauses;
    }
}
