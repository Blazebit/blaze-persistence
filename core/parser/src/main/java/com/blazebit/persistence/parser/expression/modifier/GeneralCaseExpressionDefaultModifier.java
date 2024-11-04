/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression.modifier;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.GeneralCaseExpression;

/**
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public class GeneralCaseExpressionDefaultModifier extends AbstractExpressionModifier<GeneralCaseExpressionDefaultModifier, GeneralCaseExpression> {

    public GeneralCaseExpressionDefaultModifier(GeneralCaseExpression target) {
        super(target);
    }

    @Override
    public void set(Expression expression) {
        target.setDefaultExpr(expression);
    }

    @Override
    public Expression get() {
        return target.getDefaultExpr();
    }

}
