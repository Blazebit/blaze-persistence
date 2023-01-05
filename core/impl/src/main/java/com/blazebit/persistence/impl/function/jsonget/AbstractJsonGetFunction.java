/*
 * Copyright 2014 - 2023 Blazebit.
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
package com.blazebit.persistence.impl.function.jsonget;

import com.blazebit.persistence.impl.util.JpqlFunctionUtil;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public abstract class AbstractJsonGetFunction implements JpqlFunction {

    public static final String FUNCTION_NAME = "JSON_GET";

    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return true;
    }

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return String.class;
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() < 2) {
            throw new RuntimeException("The " + FUNCTION_NAME + " function requires at least two arguments <jsonField>, <key1|arrayIndex1>, ..., <keyN|arrayIndexN>! args=" + context);
        }
        render0(context);
    }

    protected abstract void render0(FunctionRenderContext context);

    public static String toJsonPath(List<Object> pathElements, int to, boolean quotePathElements) {
        StringBuilder jsonPathBuilder = new StringBuilder("$");
        for (int i = 0; i < to; i++) {
            Object currentPathElement = pathElements.get(i);
            if (currentPathElement instanceof Integer) {
                jsonPathBuilder.append('[');
                jsonPathBuilder.append((int) currentPathElement);
                jsonPathBuilder.append(']');
            } else {
                jsonPathBuilder.append('.');
                if (quotePathElements) {
                    jsonPathBuilder.append("\"");
                }
                jsonPathBuilder.append((String) currentPathElement);
                if (quotePathElements) {
                    jsonPathBuilder.append("\"");
                }
            }
        }
        return jsonPathBuilder.toString();
    }

    public static List<Object> retrieveJsonPathElements(FunctionRenderContext context, int pathStartOffset) {
        List<Object> jsonPathElements = new ArrayList<>(context.getArgumentsSize() - pathStartOffset);
        for (int i = pathStartOffset; i < context.getArgumentsSize(); i++) {
            try {
                jsonPathElements.add(Integer.parseInt(JpqlFunctionUtil.unquoteSingleQuotes(context.getArgument(i))));
            } catch (NumberFormatException e) {
                jsonPathElements.add(JpqlFunctionUtil.unquoteDoubleQuotes(JpqlFunctionUtil.unquoteSingleQuotes(context.getArgument(i))));
            }
        }
        return jsonPathElements;
    }
}
