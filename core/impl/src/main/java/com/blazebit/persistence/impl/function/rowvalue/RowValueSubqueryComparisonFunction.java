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

package com.blazebit.persistence.impl.function.rowvalue;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 * @author Christian Beikov
 * @since 1.4.1
 */
public class RowValueSubqueryComparisonFunction implements JpqlFunction {
    public static final String FUNCTION_NAME = "compare_row_value_subquery";

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
        return int.class;
    }

    @Override
    public void render(FunctionRenderContext context) {
        String operator = context.getArgument(0);
        // need to unquote operator
        operator = operator.substring(1, operator.length() - 1);
        context.addChunk("(");
        context.addArgument(1);
        for (int argIdx = 2; argIdx < context.getArgumentsSize() - 1; argIdx++) {
            context.addChunk(", ");
            context.addArgument(argIdx);
        }

        context.addChunk(") " + operator + " (");

        context.addArgument(context.getArgumentsSize() - 1);
        context.addChunk(") and 0");
    }

}