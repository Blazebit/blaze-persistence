/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.every;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
public class FallbackEveryFunction extends EveryFunction {

    public static final FallbackEveryFunction INSTANCE = new FallbackEveryFunction();

    private FallbackEveryFunction() {
    }

    @Override
    public void render(FunctionRenderContext context) {
        context.addChunk("MIN(");
        context.addArgument(0);
        context.addChunk(")");
    }

}
