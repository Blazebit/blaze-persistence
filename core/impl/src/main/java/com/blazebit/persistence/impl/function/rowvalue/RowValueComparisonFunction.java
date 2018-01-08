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

package com.blazebit.persistence.impl.function.rowvalue;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class RowValueComparisonFunction implements JpqlFunction {
    public static final String FUNCTION_NAME = "compare_row_value";

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
        return boolean.class;
    }

    @Override
    public void render(FunctionRenderContext context) {
        String operator = context.getArgument(0);
        // need to unquote operator
        operator = operator.substring(1, operator.length() - 1);
        context.addChunk(getLeftmostChunk());
        int rowValueArity = (context.getArgumentsSize() - 1) / 2;
        addArguments(context, 1, rowValueArity + 1);
        context.addChunk(") " + operator + " (");
        addArguments(context, rowValueArity + 1, 2 * rowValueArity + 1);
        context.addChunk(getRightmostChunk());
    }

    protected String getLeftmostChunk() {
        return "((";
    }

    protected String getRightmostChunk() {
        return "))";
    }

    private void addArguments(FunctionRenderContext context, int from, int to) {
        context.addArgument(from);
        for (int i = from + 1; i < to; i++) {
            context.addChunk(", ");
            context.addArgument(i);
        }
    }

}