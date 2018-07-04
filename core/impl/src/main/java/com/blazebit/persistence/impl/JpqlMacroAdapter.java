/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.MacroFunction;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.spi.CacheableJpqlMacro;
import com.blazebit.persistence.spi.JpqlMacro;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static Map<String, MacroFunction> createMacros(Map<String, JpqlMacro> jpqlMacros, ExpressionFactory expressionFactory) {
        Map<String, MacroFunction> map = new HashMap<String, MacroFunction>(jpqlMacros.size());
        for (Map.Entry<String, JpqlMacro> entry : jpqlMacros.entrySet()) {
            map.put(entry.getKey(), new JpqlMacroAdapter(entry.getValue(), expressionFactory));
        }
        return map;
    }

    @Override
    public Expression apply(List<Expression> expressions) {
        JpqlMacroFunctionRenderContext context = new JpqlMacroFunctionRenderContext(expressions);
        macro.render(context);
        String resultExpression = context.renderToString();
        if (resultExpression.isEmpty()) {
            return new PathExpression();
        }
        return expressionFactory.createSimpleExpression(resultExpression, false);
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
