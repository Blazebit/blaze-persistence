/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression.modifier;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.predicate.LikePredicate;

/**
 *
 * @author Christian Beikov
 * @since 1.6.3
 */
public class LikePredicateEscapeModifier extends AbstractExpressionModifier<LikePredicateEscapeModifier, LikePredicate> {

    public LikePredicateEscapeModifier(LikePredicate target) {
        super(target);
    }

    @Override
    public void set(Expression expression) {
        target.setEscapeCharacter(expression);
    }

    @Override
    public Expression get() {
        return target.getEscapeCharacter();
    }

}
