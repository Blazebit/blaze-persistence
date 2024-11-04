/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.alias;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.4.1
 */
public class AliasFunction implements JpqlFunction {

    public static final String FUNCTION_NAME = "alias";

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
        context.addArgument(0);
        context.addChunk(" as ");
        String alias = context.getArgument(1);
        context.addChunk(alias.substring(1, alias.length() - 1));
    }
}
