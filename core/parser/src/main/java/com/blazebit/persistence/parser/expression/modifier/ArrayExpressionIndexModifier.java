/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression.modifier;

import com.blazebit.persistence.parser.expression.ArrayExpression;
import com.blazebit.persistence.parser.expression.Expression;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ArrayExpressionIndexModifier extends AbstractExpressionModifier<ArrayExpressionIndexModifier, ArrayExpression> {

    public ArrayExpressionIndexModifier(ArrayExpression target) {
        super(target);
    }

    @Override
    public void set(Expression expression) {
        target.setIndex(expression);
    }

    @Override
    public Expression get() {
        return target.getIndex();
    }

}
