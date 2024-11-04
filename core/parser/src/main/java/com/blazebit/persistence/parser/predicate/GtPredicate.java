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
public class GtPredicate extends QuantifiableBinaryExpressionPredicate {

    public GtPredicate() {
    }

    public GtPredicate(Expression left, Expression right) {
        this(left, right, false);
    }

    public GtPredicate(Expression left, Expression right, boolean negated) {
        this(left, right, PredicateQuantifier.ONE, negated);
    }

    public GtPredicate(Expression left, Expression right, PredicateQuantifier quantifier, boolean negated) {
        super(left, right, quantifier, negated);
    }

    @Override
    public GtPredicate copy(ExpressionCopyContext copyContext) {
        return new GtPredicate(left.copy(copyContext), right.copy(copyContext), quantifier, negated);
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
