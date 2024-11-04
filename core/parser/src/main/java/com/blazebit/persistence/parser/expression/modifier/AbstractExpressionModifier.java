/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression.modifier;

import com.blazebit.persistence.parser.expression.Expression;

/**
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractExpressionModifier<SELF extends AbstractExpressionModifier<SELF, T>, T extends Expression> implements ExpressionModifier {

    protected final T target;

    public AbstractExpressionModifier(T target) {
        this.target = target;
    }

    public T getTarget() {
        return target;
    }

}
