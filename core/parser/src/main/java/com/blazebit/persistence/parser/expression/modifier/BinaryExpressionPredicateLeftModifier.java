/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression.modifier;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.predicate.BinaryExpressionPredicate;

/**
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public class BinaryExpressionPredicateLeftModifier extends AbstractExpressionModifier<BinaryExpressionPredicateLeftModifier, BinaryExpressionPredicate> {

    public BinaryExpressionPredicateLeftModifier(BinaryExpressionPredicate target) {
        super(target);
    }

    @Override
    public void set(Expression expression) {
        target.setLeft(expression);
    }

    @Override
    public Expression get() {
        return target.getLeft();
    }

}
