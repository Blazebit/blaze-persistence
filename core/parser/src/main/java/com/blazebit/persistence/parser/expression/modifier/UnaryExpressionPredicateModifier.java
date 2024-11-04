/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression.modifier;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.predicate.UnaryExpressionPredicate;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public class UnaryExpressionPredicateModifier extends AbstractExpressionModifier<UnaryExpressionPredicateModifier, UnaryExpressionPredicate> {

    public UnaryExpressionPredicateModifier(UnaryExpressionPredicate target) {
        super(target);
    }

    @Override
    public void set(Expression expression) {
        target.setExpression(expression);
    }

    @Override
    public Expression get() {
        return target.getExpression();
    }

}
