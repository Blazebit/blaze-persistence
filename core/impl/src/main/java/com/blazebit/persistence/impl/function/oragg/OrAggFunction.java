/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.oragg;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
public class OrAggFunction implements JpqlFunction {

    public static final String FUNCTION_NAME = "ANY";
    public static final OrAggFunction INSTANCE = new OrAggFunction();

    protected OrAggFunction() {
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
        return Boolean.class;
    }

    @Override
    public void render(FunctionRenderContext context) {
        context.addChunk("BOOL_OR(");
        context.addArgument(0);
        context.addChunk(")");
    }

}
