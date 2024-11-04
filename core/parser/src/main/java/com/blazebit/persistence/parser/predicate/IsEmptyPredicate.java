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
public class IsEmptyPredicate extends UnaryExpressionPredicate {

    public IsEmptyPredicate(Expression expression) {
        super(expression);
    }

    public IsEmptyPredicate(Expression expression, boolean negated) {
        super(expression, negated);
    }

    @Override
    public IsEmptyPredicate copy(ExpressionCopyContext copyContext) {
        return new IsEmptyPredicate(expression.copy(copyContext), negated);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
