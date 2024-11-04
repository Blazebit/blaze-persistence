/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.coltrunc;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.4.1
 */
public class ColumnTruncFunction implements JpqlFunction {

    public static final String SYNTHETIC_COLUMN_PREFIX = "synth_col_";
    public static final String FUNCTION_NAME = "column_trunc";

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
        return firstArgumentType;
    }

    @Override
    public void render(FunctionRenderContext context) {
        int size = context.getArgumentsSize();
        if (size != 2) {
            throw new RuntimeException("The " + FUNCTION_NAME + " function needs exactly two arguments!");
        }
        int columnCount = Integer.parseInt(context.getArgument(1).trim());
        context.addChunk("(select " + SYNTHETIC_COLUMN_PREFIX + "0");
        for (int i = 1; i < columnCount; i++) {
            context.addChunk(", " + SYNTHETIC_COLUMN_PREFIX + i);
        }
        context.addChunk(" from (");
        context.addArgument(0);
        context.addChunk(") tmp)");
    }
}
