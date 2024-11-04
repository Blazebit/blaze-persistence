/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression.modifier;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.TreatExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TreatExpressionModifier extends AbstractExpressionModifier<TreatExpressionModifier, TreatExpression> {

    public TreatExpressionModifier(TreatExpression target) {
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
