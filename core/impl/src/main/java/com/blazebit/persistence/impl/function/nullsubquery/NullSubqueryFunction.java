/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.nullsubquery;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class NullSubqueryFunction implements JpqlFunction {

    public static final String FUNCTION_NAME = "null_subquery";

    private final String fromDual;

    public NullSubqueryFunction(String fromDual) {
        this.fromDual = fromDual;
    }

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
        context.addChunk("(select null");
        if (fromDual != null) {
            context.addChunk(fromDual);
        }
        context.addChunk(")");
    }
}
