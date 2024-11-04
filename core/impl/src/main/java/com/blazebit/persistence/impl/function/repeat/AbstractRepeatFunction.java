/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.repeat;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.TemplateRenderer;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractRepeatFunction implements JpqlFunction {

    public static final String FUNCTION_NAME = "repeat";
    protected final TemplateRenderer renderer;

    public AbstractRepeatFunction(String template) {
        this.renderer = new TemplateRenderer(template);
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
        return String.class;
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() != 2) {
            throw new RuntimeException("The " + FUNCTION_NAME + " function needs exactly two argument!");
        }
        renderer.start(context).addArgument(0).addArgument(1).build();
    }
}
