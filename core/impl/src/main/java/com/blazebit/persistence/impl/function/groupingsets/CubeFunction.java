/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.groupingsets;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
public class CubeFunction implements JpqlFunction {

    public static final String FUNCTION_NAME = "cube";

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
        return Integer.class;
    }

    @Override
    public void render(FunctionRenderContext functionRenderContext) {
        int argumentsSize = functionRenderContext.getArgumentsSize();
        functionRenderContext.addChunk("cube (");
        if (argumentsSize != 0) {
            functionRenderContext.addArgument(0);
            for (int i = 1; i < argumentsSize; i++) {
                functionRenderContext.addChunk(",");
                functionRenderContext.addArgument(i);
            }
        }
        functionRenderContext.addChunk(")");
    }

}
