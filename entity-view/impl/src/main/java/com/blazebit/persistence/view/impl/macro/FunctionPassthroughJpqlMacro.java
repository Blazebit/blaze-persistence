/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.macro;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlMacro;

/**
 * This is a macro used just for type validation.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class FunctionPassthroughJpqlMacro implements JpqlMacro {

    private final String name;

    public FunctionPassthroughJpqlMacro(String name) {
        this.name = "FUNCTION('" + name + "'";
    }

    @Override
    public void render(FunctionRenderContext context) {
        context.addChunk(name);
        int size = context.getArgumentsSize();
        for (int i = 0; i < size; i++) {
            context.addChunk(", ");
            context.addArgument(i);
        }
        context.addChunk(")");
    }
}
