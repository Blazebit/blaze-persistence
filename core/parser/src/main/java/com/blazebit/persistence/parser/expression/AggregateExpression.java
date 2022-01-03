/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.parser.expression;

import com.blazebit.persistence.parser.predicate.Predicate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class AggregateExpression extends FunctionExpression {

    private boolean distinct;

    public AggregateExpression(boolean distinct, String functionName, List<Expression> expressions) {
        super(functionName, expressions);
        this.distinct = distinct;
    }

    public AggregateExpression(boolean distinct, String functionName, List<Expression> expressions, List<OrderByItem> withinGroup, Predicate filterPredicate) {
        super(functionName, expressions, withinGroup, filterPredicate == null ? null : new WindowDefinition(null, filterPredicate));
        this.distinct = distinct;
    }

    @Override
    public AggregateExpression copy(ExpressionCopyContext copyContext) {
        int size = expressions.size();
        List<Expression> newExpressions;
        if (size == 0) {
            newExpressions = Collections.emptyList();
        } else {
            newExpressions = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                newExpressions.add(expressions.get(i).copy(copyContext));
            }
        }
        List<OrderByItem> newWithinGroup;
        if (withinGroup == null) {
            newWithinGroup = null;
        } else {
            size = withinGroup.size();
            newWithinGroup = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                newWithinGroup.add(withinGroup.get(i).copy(copyContext));
            }
        }
        return new AggregateExpression(distinct, functionName, newExpressions, newWithinGroup, windowDefinition == null ? null : windowDefinition.getFilterPredicate().copy(copyContext));
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }
}
