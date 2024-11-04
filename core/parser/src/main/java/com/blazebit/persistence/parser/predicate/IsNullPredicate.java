/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.predicate;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class IsNullPredicate extends UnaryExpressionPredicate {

    public IsNullPredicate(Expression expression) {
        super(expression);
    }

    public IsNullPredicate(Expression expression, boolean negated) {
        super(expression, negated);
    }

    @Override
    public IsNullPredicate copy(ExpressionCopyContext copyContext) {
        return new IsNullPredicate(expression.copy(copyContext), negated);
    }

    @Override
    public void accept(Expression.Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(Expression.ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
