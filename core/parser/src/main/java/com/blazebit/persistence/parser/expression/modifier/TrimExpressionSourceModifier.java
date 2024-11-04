/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression.modifier;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.TrimExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TrimExpressionSourceModifier extends AbstractExpressionModifier<TrimExpressionSourceModifier, TrimExpression> {

    public TrimExpressionSourceModifier(TrimExpression target) {
        super(target);
    }

    @Override
    public void set(Expression expression) {
        target.setTrimSource(expression);
    }

    @Override
    public Expression get() {
        return target.getTrimSource();
    }

}
