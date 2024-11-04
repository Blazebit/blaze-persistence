/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression.modifier;

import com.blazebit.persistence.parser.expression.ArithmeticExpression;
import com.blazebit.persistence.parser.expression.Expression;

/**
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ArithmeticLeftExpressionModifier extends AbstractExpressionModifier<ArithmeticLeftExpressionModifier, ArithmeticExpression> {

    public ArithmeticLeftExpressionModifier(ArithmeticExpression target) {
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
