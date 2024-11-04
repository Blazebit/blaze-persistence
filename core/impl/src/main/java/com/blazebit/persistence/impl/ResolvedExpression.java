/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.expression.Expression;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class ResolvedExpression {

    private final String expressionString;
    private final Expression expression;

    public ResolvedExpression(String expressionString, Expression expression) {
        this.expressionString = expressionString;
        this.expression = expression;
    }

    public String getExpressionString() {
        return expressionString;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResolvedExpression)) {
            return false;
        }

        return expressionString.equals(((ResolvedExpression) o).expressionString);
    }

    @Override
    public int hashCode() {
        return expressionString.hashCode();
    }

    @Override
    public String toString() {
        return expressionString;
    }
}
