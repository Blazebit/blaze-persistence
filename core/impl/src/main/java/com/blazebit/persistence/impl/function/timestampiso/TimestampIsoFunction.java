/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.timestampiso;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 * @author Christian Beikov
 * @since 1.6.12
 */
public class TimestampIsoFunction implements JpqlFunction {
    public static final String FUNCTION_NAME = "timestamp_iso";

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
            throw new RuntimeException("The timestamp_iso function needs exactly one argument <timestamp>! args=" + context);
        }
        context.addChunk( getExpression( context.getArgument( 0)));
    }

    protected String getExpression(String timestampArgument) {
        return "to_char(" + timestampArgument + ", 'YYYY-MM-DD\"T\"HH24:MI:SS.FF9')";
    }

}