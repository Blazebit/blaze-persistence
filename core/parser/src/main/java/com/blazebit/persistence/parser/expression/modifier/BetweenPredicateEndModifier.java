/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression.modifier;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.predicate.BetweenPredicate;

/**
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public class BetweenPredicateEndModifier extends AbstractExpressionModifier<BetweenPredicateEndModifier, BetweenPredicate> {

    public BetweenPredicateEndModifier(BetweenPredicate target) {
        super(target);
    }

    @Override
    public void set(Expression expression) {
        target.setEnd(expression);
    }

    @Override
    public Expression get() {
        return target.getEnd();
    }

}
