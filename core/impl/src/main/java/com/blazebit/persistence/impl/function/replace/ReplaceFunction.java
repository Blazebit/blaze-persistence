/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.replace;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class ReplaceFunction implements JpqlFunction {
    public static final String FUNCTION_NAME = "replace";

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
        if (context.getArgumentsSize() != 3) {
            throw new RuntimeException("The replace function needs exactly three arguments <string>, <search> and <replacement>! args=" + context);
        }
        context.addChunk(getReplaceString(context.getArgument(0), context.getArgument(1), context.getArgument(2)));
    }

    public String getReplaceString(String argument, String search, String replace) {
        return "replace(" + argument + "," + search + "," + replace + ")";
    }

}