/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jpa.function;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CountStarFunction implements JpqlFunction {
    public static final String FUNCTION_NAME = "count_star";

    @Override
    public boolean hasArguments() {
        return false;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return true;
    }

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return long.class;
    }

    @Override
    public void render(FunctionRenderContext context) {
        context.addChunk("COUNT(*)");
    }

}
