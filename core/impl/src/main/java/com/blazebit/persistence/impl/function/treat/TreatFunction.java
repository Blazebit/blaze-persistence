/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.treat;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TreatFunction implements JpqlFunction {
    
    private final Class<?> castType;

    public TreatFunction(Class<?> castType) {
        this.castType = castType;
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
        return castType;
    }

    @Override
    public void render(FunctionRenderContext context) {
        context.addArgument(0);
    }

}
