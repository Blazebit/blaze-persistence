/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression.modifier;

import com.blazebit.persistence.parser.expression.ArithmeticFactor;
import com.blazebit.persistence.parser.expression.Expression;

/**
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ArithmeticFactorExpressionModifier extends AbstractExpressionModifier<ArithmeticFactorExpressionModifier, ArithmeticFactor> {

    public ArithmeticFactorExpressionModifier(ArithmeticFactor target) {
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
