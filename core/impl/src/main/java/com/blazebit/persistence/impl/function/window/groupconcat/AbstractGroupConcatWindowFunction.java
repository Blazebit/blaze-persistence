/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.impl.function.window.groupconcat;

import com.blazebit.persistence.impl.function.Order;
import com.blazebit.persistence.impl.function.window.AbstractWindowFunction;
import com.blazebit.persistence.parser.util.TypeUtils;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public abstract class AbstractGroupConcatWindowFunction extends AbstractWindowFunction implements JpqlFunction {

    public AbstractGroupConcatWindowFunction(String functionName, boolean nullIsSmallest, boolean supportsNullPrecedence, boolean supportsFilterClause, boolean allowsFilterClause) {
        super(functionName, nullIsSmallest, supportsNullPrecedence, supportsFilterClause, allowsFilterClause);
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

    @Override
    protected GroupConcat getWindowFunction(FunctionRenderContext context) {
        if (context.getArgumentsSize() == 0) {
            throw new RuntimeException("The group concat function needs at least one argument! args=" + context);
        }

        return getWindowFunction(context, new GroupConcat(functionName), 0);
    }

    @Override
    protected Enum<?> processArgument(Enum<?> mode, WindowFunction windowFunction, String argument) {
        if (mode == Mode.SEPARATOR) {
            if (!"'SEPARATOR'".equalsIgnoreCase(argument)) {
                GroupConcat groupConcat = (GroupConcat) windowFunction;
                if (groupConcat.separator != null) {
                    throw new IllegalArgumentException("Illegal multiple separators for group concat '" + argument + "'. Expected 'ORDER BY'!");
                }

                groupConcat.separator = argument.substring(argument.indexOf('\'') + 1, argument.lastIndexOf('\''));
            }
            return null;
        } else {
            super.processArgument(mode, windowFunction, argument);
            return Mode.SEPARATOR;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.4.0
     */
    private enum Mode {
        SEPARATOR,
        ORDER_BY
    }

    protected void render(StringBuilder sb, Order order) {
        sb.append(order.getExpression());
        
        if (order.isAscending()) {
            sb.append(" ASC");
        } else {
            sb.append(" DESC");
        }
        
        if (order.isNullsFirst()) {
            sb.append(" NULLS FIRST");
        } else {
            sb.append(" NULLS LAST");
        }
    }
    
    protected String quoted(String s) {
        StringBuilder sb = new StringBuilder();
        TypeUtils.STRING_CONVERTER.appendTo(s, sb);
        return sb.toString();
    }

    /**
     * @author Christian Beikov
     * @since 1.4.0
     */
    protected static final class GroupConcat extends WindowFunction {

        private String separator;

        public GroupConcat(String functionName) {
            super(functionName);
        }

        public String getSeparator() {
            return separator == null ? "," : separator;
        }
    }

}
