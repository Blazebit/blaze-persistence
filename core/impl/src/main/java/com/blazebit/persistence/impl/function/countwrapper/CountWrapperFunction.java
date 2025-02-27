/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.countwrapper;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class CountWrapperFunction implements JpqlFunction {

    public static final String FUNCTION_NAME = "count_wrapper";

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
        return Long.class;
    }

    @Override
    public void render(FunctionRenderContext context) {
        int size = context.getArgumentsSize();
        if (size != 1) {
            throw new RuntimeException("The " + FUNCTION_NAME + " function needs exactly one argument!");
        }
        context.addChunk("(select count(*) from (");
        context.addArgument(0);
        context.addChunk(") tmp)");
    }
}
