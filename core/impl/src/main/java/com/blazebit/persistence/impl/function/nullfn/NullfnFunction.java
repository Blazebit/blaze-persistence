/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.nullfn;

import com.blazebit.persistence.impl.util.JpqlFunctionUtil;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.4.1
 */
public class NullfnFunction implements JpqlFunction {

    public static final String FUNCTION_NAME = "nullfn";

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
        if (firstArgumentType == null) {
            return Integer.class;
        }
        return firstArgumentType;
    }

    @Override
    public void render(FunctionRenderContext functionRenderContext) {
        if (functionRenderContext.getArgumentsSize() > 1) {
            functionRenderContext.addChunk("cast(");
        }
        functionRenderContext.addChunk("null");
        if (functionRenderContext.getArgumentsSize() > 1) {
            functionRenderContext.addChunk(" as ");
            functionRenderContext.addChunk(JpqlFunctionUtil.unquoteSingleQuotes(functionRenderContext.getArgument(1)));
            functionRenderContext.addChunk(")");
        }
    }

}
