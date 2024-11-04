/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.predicate;

import com.blazebit.persistence.parser.expression.AbstractExpression;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public abstract class AbstractPredicate extends AbstractExpression implements Predicate {

    protected boolean negated;

    public AbstractPredicate(boolean negated) {
        this.negated = negated;
    }

    @Override
    public boolean isNegated() {
        return negated;
    }

    @Override
    public void setNegated(boolean negated) {
        this.negated = negated;
    }

    @Override
    public void negate() {
        this.negated = !this.negated;
    }

    @Override
    public abstract Predicate copy(ExpressionCopyContext copyContext);

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractPredicate)) {
            return false;
        }

        AbstractPredicate that = (AbstractPredicate) o;

        return negated == that.negated;

    }

    @Override
    public int hashCode() {
        return (negated ? 1 : 0);
    }
}
