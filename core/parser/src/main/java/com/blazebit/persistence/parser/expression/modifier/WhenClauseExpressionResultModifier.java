/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression.modifier;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.WhenClauseExpression;

/**
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public class WhenClauseExpressionResultModifier extends AbstractExpressionModifier<WhenClauseExpressionResultModifier, WhenClauseExpression> {

    public WhenClauseExpressionResultModifier(WhenClauseExpression target) {
        super(target);
    }

    @Override
    public void set(Expression expression) {
        target.setResult(expression);
    }

    @Override
    public Expression get() {
        return target.getResult();
    }

}
