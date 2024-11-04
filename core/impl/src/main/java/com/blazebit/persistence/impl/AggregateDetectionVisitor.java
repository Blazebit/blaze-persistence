/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.expression.AbortableVisitorAdapter;
import com.blazebit.persistence.parser.expression.AggregateExpression;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.FunctionExpression;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
class AggregateDetectionVisitor extends AbortableVisitorAdapter {

    public static final Expression.ResultVisitor<Boolean> INSTANCE = new AggregateDetectionVisitor();

    private AggregateDetectionVisitor() {
    }

    @Override
    public Boolean visit(FunctionExpression expression) {
        if (expression instanceof AggregateExpression) {
            return true;
        }
        return super.visit(expression);
    }
}