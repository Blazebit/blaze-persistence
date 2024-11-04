/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.concat;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ConcatFunction implements JpqlFunction {

    public static final String FUNCTION_NAME = "concat";
    public static final ConcatFunction INSTANCE = new ConcatFunction();

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
        context.addChunk("concat(");
        context.addArgument(0);
        for (int i = 1; i < context.getArgumentsSize(); i++) {
            context.addChunk(",");
            context.addArgument(i);
        }
        context.addChunk(")");
    }

    public String startConcat() {
        return "concat(";
    }

    public String endConcat() {
        return ")";
    }

    public String concatSeparator() {
        return ",";
    }
}
