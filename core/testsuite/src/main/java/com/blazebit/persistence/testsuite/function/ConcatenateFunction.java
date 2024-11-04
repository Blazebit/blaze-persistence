/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.function;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ConcatenateFunction implements JpqlFunction {
    
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
        context.addChunk("concat(");
        context.addArgument(0);
        
        for (int i = 1; i < context.getArgumentsSize(); i++) {
            context.addChunk(",");
            context.addArgument(i);
        }
        
        context.addChunk(")");
    }
    
}
