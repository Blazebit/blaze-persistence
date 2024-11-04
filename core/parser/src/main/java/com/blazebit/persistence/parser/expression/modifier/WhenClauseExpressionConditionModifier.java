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
public class WhenClauseExpressionConditionModifier extends AbstractExpressionModifier<WhenClauseExpressionConditionModifier, WhenClauseExpression> {

    public WhenClauseExpressionConditionModifier(WhenClauseExpression target) {
        super(target);
    }

    @Override
    public void set(Expression expression) {
        target.setCondition(expression);
    }

    @Override
    public Expression get() {
        return target.getCondition();
    }

}
