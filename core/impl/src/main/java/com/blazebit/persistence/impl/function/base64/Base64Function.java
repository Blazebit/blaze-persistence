/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.base64;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class Base64Function implements JpqlFunction {
    public static final String FUNCTION_NAME = "base64";

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
        if (context.getArgumentsSize() != 1) {
            throw new RuntimeException("The base64 function needs one argument <bytes>! args=" + context);
        }
        context.addChunk(getEncodedString(context.getArgument(0)));
    }

    public String getEncodedString(String bytes) {
        return "base64(" + bytes + ")";
    }

}