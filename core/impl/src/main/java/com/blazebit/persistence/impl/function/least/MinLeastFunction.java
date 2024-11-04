/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.least;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MinLeastFunction extends AbstractLeastFunction {

    @Override
    public void render(FunctionRenderContext context) {
        int size = context.getArgumentsSize();
        if (size < 2) {
            throw new RuntimeException("The " + FUNCTION_NAME + " function needs at least two argument!");
        }
        context.addChunk("min(");
        context.addArgument(0);
        for (int i = 1; i < size; i++) {
            context.addChunk(", ");
            context.addArgument(i);
        }
        context.addChunk(")");
    }

}
