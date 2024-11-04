/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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