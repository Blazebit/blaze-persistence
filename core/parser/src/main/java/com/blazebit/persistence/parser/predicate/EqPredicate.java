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
public class EqPredicate extends QuantifiableBinaryExpressionPredicate implements Negatable {

    public EqPredicate(boolean negated) {
        this(null, null, negated);
    }

    public EqPredicate(Expression left, Expression right) {
        this(left, right, PredicateQuantifier.ONE, false);
    }

    public EqPredicate(Expression left, Expression right, boolean negated) {
        this(left, right, PredicateQuantifier.ONE, negated);
    }

    public EqPredicate(Expression left, Expression right, PredicateQuantifier quantifier) {
        this(left, right, quantifier, false);
    }

    public EqPredicate(Expression left, Expression right, PredicateQuantifier quantifier, boolean negated) {
        super(left, right, quantifier, negated);
    }

    @Override
    public EqPredicate copy(ExpressionCopyContext copyContext) {
        return new EqPredicate(left.copy(copyContext), right.copy(copyContext), quantifier, negated);
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
