/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.oragg;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
public class FallbackOrAggFunction extends OrAggFunction {

    public static final FallbackOrAggFunction INSTANCE = new FallbackOrAggFunction();

    private FallbackOrAggFunction() {
    }

    @Override
    public void render(FunctionRenderContext context) {
        context.addChunk("MAX(");
        context.addArgument(0);
        context.addChunk(")");
    }

}
