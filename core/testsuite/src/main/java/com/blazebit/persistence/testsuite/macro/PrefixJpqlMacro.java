/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.macro;

import com.blazebit.persistence.spi.CacheableJpqlMacro;
import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PrefixJpqlMacro implements CacheableJpqlMacro {

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() != 2) {
            throw new IllegalArgumentException("The prefix macro requires exactly two arguments: <prefix> and <expression>!");
        }

        context.addArgument(0);
        context.addChunk(".");
        context.addArgument(1);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == PrefixJpqlMacro.class;
    }
}
