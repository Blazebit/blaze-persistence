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
public class GePredicate extends QuantifiableBinaryExpressionPredicate {

    public GePredicate() {
    }

    public GePredicate(Expression left, Expression right) {
        this(left, right, false);
    }

    public GePredicate(Expression left, Expression right, boolean negated) {
        this(left, right, PredicateQuantifier.ONE, negated);
    }

    public GePredicate(Expression left, Expression right, PredicateQuantifier quantifier, boolean negated) {
        super(left, right, quantifier, negated);
    }

    @Override
    public GePredicate copy(ExpressionCopyContext copyContext) {
        return new GePredicate(left.copy(copyContext), right.copy(copyContext), quantifier, negated);
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
