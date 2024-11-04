/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.greatest;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SelectMaxUnionGreatestFunction extends AbstractGreatestFunction {

    @Override
    public void render(FunctionRenderContext context) {
        int size = context.getArgumentsSize();
        if (size < 2) {
            throw new RuntimeException("The " + FUNCTION_NAME + " function needs at least two argument!");
        }
        context.addChunk("(select max(c) from (select ");
        context.addArgument(0);
        context.addChunk(" as c");
        for (int i = 1; i < size; i++) {
            context.addChunk(" union all select ");
            context.addArgument(i);
        }
        context.addChunk(") T)");
    }

}
