/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
