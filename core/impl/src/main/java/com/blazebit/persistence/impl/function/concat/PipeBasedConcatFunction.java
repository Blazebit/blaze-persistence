/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.concat;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class PipeBasedConcatFunction extends ConcatFunction {

    public static final ConcatFunction INSTANCE = new PipeBasedConcatFunction();

    @Override
    public void render(FunctionRenderContext context) {
        context.addArgument(0);
        for (int i = 1; i < context.getArgumentsSize(); i++) {
            context.addChunk("||");
            context.addArgument(i);
        }
    }

    @Override
    public String startConcat() {
        return "(";
    }

    @Override
    public String endConcat() {
        return ")";
    }

    @Override
    public String concatSeparator() {
        return "||";
    }
}
