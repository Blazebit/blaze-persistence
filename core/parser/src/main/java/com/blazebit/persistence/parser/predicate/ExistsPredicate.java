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
public class ExistsPredicate extends UnaryExpressionPredicate {

    public ExistsPredicate() {
        super(null);
    }

    public ExistsPredicate(boolean negated) {
        super(null, negated);
    }

    public ExistsPredicate(Expression expression, boolean negated) {
        super(expression, negated);
    }

    @Override
    public ExistsPredicate copy(ExpressionCopyContext copyContext) {
        return new ExistsPredicate(expression.copy(copyContext), negated);
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
