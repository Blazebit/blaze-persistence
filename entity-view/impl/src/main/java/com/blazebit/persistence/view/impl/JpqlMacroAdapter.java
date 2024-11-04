/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.MacroFunction;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.spi.CacheableJpqlMacro;
import com.blazebit.persistence.spi.JpqlMacro;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class JpqlMacroAdapter implements MacroFunction {

    private final JpqlMacro macro;
    private final ExpressionFactory expressionFactory;
    private final Object[] state;

    public JpqlMacroAdapter(JpqlMacro macro, ExpressionFactory expressionFactory) {
        this.macro = macro;
        this.expressionFactory = expressionFactory;
        this.state = new Object[]{ macro, expressionFactory };
    }

    @Override
    public Expression apply(List<Expression> expressions) {
        JpqlMacroFunctionRenderContext context = new JpqlMacroFunctionRenderContext(expressions);
        macro.render(context);
        String resultExpression = context.renderToString();
        if (resultExpression.isEmpty()) {
            return new PathExpression();
        }
        return expressionFactory.createSimpleExpression(resultExpression, false, false, true);
    }

    @Override
    public Object[] getState() {
        return state;
    }

    @Override
    public boolean supportsCaching() {
        return macro instanceof CacheableJpqlMacro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JpqlMacroAdapter)) {
            return false;
        }

        JpqlMacroAdapter that = (JpqlMacroAdapter) o;

        return Arrays.equals(getState(), that.getState());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getState());
    }
}
