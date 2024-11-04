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
public class BinaryExpressionPredicateRightModifier extends AbstractExpressionModifier<BinaryExpressionPredicateRightModifier, BinaryExpressionPredicate> {

    public BinaryExpressionPredicateRightModifier(BinaryExpressionPredicate target) {
        super(target);
    }

    @Override
    public void set(Expression expression) {
        target.setRight(expression);
    }

    @Override
    public Expression get() {
        return target.getLeft();
    }

}
