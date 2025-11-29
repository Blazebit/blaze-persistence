/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.inwrapper;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 *
 * @author Christian Beikov
 * @since 2.0.0
 */
public class InWrapperFunction implements JpqlFunction {

    public static final String FUNCTION_NAME = "in_wrapper";

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
            throw new RuntimeException("The " + FUNCTION_NAME + " function needs exactly two argument!");
        }
        context.addChunk("1 and ");
        context.addArgument(0);
        context.addChunk(" in (select * from ");
        context.addArgument(1);
        context.addChunk(" tmp_)");
    }
}
