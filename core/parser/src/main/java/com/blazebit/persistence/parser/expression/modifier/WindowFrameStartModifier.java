/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression.modifier;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.WindowDefinition;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class WindowFrameStartModifier implements ExpressionModifier {

    protected final WindowDefinition windowDefinition;

    public WindowFrameStartModifier(WindowDefinition windowDefinition) {
        this.windowDefinition = windowDefinition;
    }

    @Override
    public void set(Expression expression) {
        windowDefinition.setFrameStartExpression(expression);
    }

    @Override
    public Expression get() {
        return windowDefinition.getFrameStartExpression();
    }

}
