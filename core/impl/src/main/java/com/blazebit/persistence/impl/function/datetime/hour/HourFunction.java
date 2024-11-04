/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.hour;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.TemplateRenderer;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class HourFunction implements JpqlFunction {

    private final TemplateRenderer renderer;

    public HourFunction() {
        this.renderer = new TemplateRenderer("extract(hour from ?1)");
    }

    public HourFunction(String template) {
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
        return Integer.class;
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() != 1) {
            throw new RuntimeException("The hour function needs exactly one argument <datetime>! args=" + context);
        }

        renderer.start(context).addArgument(0).build();
    }
}
