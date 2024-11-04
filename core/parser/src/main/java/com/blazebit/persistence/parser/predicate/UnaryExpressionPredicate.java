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
public abstract class UnaryExpressionPredicate extends AbstractPredicate {

    protected Expression expression;

    public UnaryExpressionPredicate(Expression expression, boolean negated) {
        super(negated);
        this.expression = expression;
        this.negated = negated;
    }

    public UnaryExpressionPredicate(Expression expression) {
        this(expression, false);
    }

    @Override
    public abstract UnaryExpressionPredicate copy(ExpressionCopyContext copyContext);

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UnaryExpressionPredicate)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        UnaryExpressionPredicate that = (UnaryExpressionPredicate) o;

        return expression != null ? expression.equals(that.expression) : that.expression == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (expression != null ? expression.hashCode() : 0);
        return result;
    }
}
