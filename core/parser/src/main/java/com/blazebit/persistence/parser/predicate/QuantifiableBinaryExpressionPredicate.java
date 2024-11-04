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
public abstract class QuantifiableBinaryExpressionPredicate extends BinaryExpressionPredicate {

    protected PredicateQuantifier quantifier;

    public QuantifiableBinaryExpressionPredicate() {
        this(null, null, false);
    }

    public QuantifiableBinaryExpressionPredicate(Expression left, Expression right, boolean negated) {
        this(left, right, PredicateQuantifier.ONE, negated);
    }

    public QuantifiableBinaryExpressionPredicate(Expression left, Expression right, PredicateQuantifier quantifier, boolean negated) {
        super(left, right, negated);
        this.quantifier = quantifier;
    }

    @Override
    public abstract QuantifiableBinaryExpressionPredicate copy(ExpressionCopyContext copyContext);

    public PredicateQuantifier getQuantifier() {
        return quantifier;
    }
    
    
    public void setQuantifier(PredicateQuantifier quantifier) {
        this.quantifier = quantifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QuantifiableBinaryExpressionPredicate)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        QuantifiableBinaryExpressionPredicate that = (QuantifiableBinaryExpressionPredicate) o;

        return quantifier == that.quantifier;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (quantifier != null ? quantifier.hashCode() : 0);
        return result;
    }
}
