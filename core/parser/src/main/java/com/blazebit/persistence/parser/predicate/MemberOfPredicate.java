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
public class MemberOfPredicate extends BinaryExpressionPredicate {

    public MemberOfPredicate(Expression left, Expression right) {
        super(left, right);
    }

    public MemberOfPredicate(Expression left, Expression right, boolean negated) {
        super(left, right, negated);
    }

    @Override
    public MemberOfPredicate copy(ExpressionCopyContext copyContext) {
        return new MemberOfPredicate(left.copy(copyContext), right.copy(copyContext), negated);
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
