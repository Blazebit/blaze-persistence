/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.literal;

import com.blazebit.persistence.impl.util.JpqlFunctionUtil;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
public abstract class LiteralFunction implements JpqlFunction {

    protected static Class<?> loadClass(String className) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            // Ignore
        }
        return clazz;
    }

    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return true;
    }

    protected abstract String getFunctionName();

    @Override
    public void render(FunctionRenderContext functionRenderContext) {
        if (functionRenderContext.getArgumentsSize() != 1) {
            throw new RuntimeException("The " + getFunctionName() + " function needs exactly one argument <jdbc_timestamp_literal>! args=" + functionRenderContext);
        }
        functionRenderContext.addChunk(JpqlFunctionUtil.unquoteSingleQuotes(functionRenderContext.getArgument(0)));
    }

}
