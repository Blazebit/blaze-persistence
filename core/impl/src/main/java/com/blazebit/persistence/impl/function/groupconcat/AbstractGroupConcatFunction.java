/*
 * Copyright 2015 Blazebit.
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
package com.blazebit.persistence.impl.function.groupconcat;

import com.blazebit.persistence.impl.function.TemplateRenderer;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractGroupConcatFunction implements JpqlFunction {

    protected final TemplateRenderer renderer;

    public AbstractGroupConcatFunction(String template) {
        this.renderer = new TemplateRenderer(template);
    }

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

    protected GroupConcat getGroupConcat(FunctionRenderContext context) {
        if (context.getArgumentsSize() == 0) {
            throw new RuntimeException("The group concat function needs at least one argument! args=" + context);
        }

        boolean distinct = false;
        String expression;
        int startIndex = 0;
        int argsSize = context.getArgumentsSize();
        String maybeDistinct = context.getArgument(0);

        if ("distinct".equalsIgnoreCase(maybeDistinct)) {
            distinct = true;
            startIndex++;
        }

        if (startIndex >= argsSize) {
            throw new RuntimeException("The group concat function needs at least one expression to concatenate! args=" + context);
        }

        expression = context.getArgument(startIndex);

        String separator = null;
        StringBuilder orderSb = new StringBuilder();
        boolean orderBy = false;

        for (int i = startIndex + 1; i < argsSize; i++) {
            String argument = context.getArgument(i);
            if ("SEPARATOR".equalsIgnoreCase(argument)) {
                orderBy = false;
            } else if ("ORDER BY".equalsIgnoreCase(argument)) {
                orderBy = true;
            } else {
                if (orderBy) {
                    if (isOrderType(argument)) {
                        orderSb.append(' ');
                        orderSb.append(argument);
                    } else {
                        if (orderSb.length() > 0) {
                            orderSb.append(',');
                        }

                        orderSb.append(argument);
                    }
                } else {
                    if (separator != null) {
                        throw new RuntimeException("Multple separators given in arguments for group concat! args=" + context);
                    }

                    separator = argument;
                }
            }
        }

        if (separator == null) {
            separator = ",";
        }

        return new GroupConcat(distinct, expression, orderSb.toString(), separator);
    }

    private static boolean isOrderType(String s) {
        String type = s.trim().toUpperCase();
        return "ASC".equals(type) || "DESC".equals(type) || "ASC NULLS FIRST".equals(type) || "ASC NULLS LAST".equals(type)
            || "DESC NULLS FIRST".equals(type) || "DESC NULLS LAST".equals(type);
    }

    protected static final class GroupConcat {

        private final boolean distinct;
        private final String expression;
        private final String orderByExpression;
        private final String separator;

        public GroupConcat(boolean distinct, String expression, String orderByExpression, String separator) {
            this.distinct = distinct;
            this.expression = expression;
            this.orderByExpression = orderByExpression;
            this.separator = separator;
        }

        public boolean isDistinct() {
            return distinct;
        }

        public String getExpression() {
            return expression;
        }

        public String getOrderByExpression() {
            return orderByExpression;
        }

        public String getSeparator() {
            return separator;
        }
    }
}
