/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.exist;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.4.1
 */
public class ExistFunction implements JpqlFunction {

    public static final String FUNCTION_NAME = "exist";

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
        if (functionRenderContext.getArgumentsSize() == 2) {
            functionRenderContext.addChunk("1 and not exists");
        } else {
            functionRenderContext.addChunk("1 and exists");
        }
        functionRenderContext.addArgument(0);
    }

}
