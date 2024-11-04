/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.TemplateRenderer;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public abstract class TruncFunction implements JpqlFunction {

    protected final TemplateRenderer renderer;

    private final String functionName;

    public TruncFunction(String functionName, String template) {
        this.functionName = functionName;
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
        return firstArgumentType;
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() != 1) {
            throw new RuntimeException("The " + functionName + " function needs exactly one argument <datetime>! args=" + context);
        }

        renderDiff(context);
    }

    protected void renderDiff(FunctionRenderContext context) {
        renderer.start(context).addArgument(0).build();
    }
}
