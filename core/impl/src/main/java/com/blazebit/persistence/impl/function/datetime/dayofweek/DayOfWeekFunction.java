/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.dayofweek;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.TemplateRenderer;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DayOfWeekFunction implements JpqlFunction {

    private final TemplateRenderer renderer;

    public DayOfWeekFunction() {
        this.renderer = new TemplateRenderer("extract(dow from ?1)");
    }

    public DayOfWeekFunction(String template) {
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
            throw new RuntimeException("The day function needs exactly one argument <datetime>! args=" + context);
        }

        renderer.start(context).addArgument(0).build();
    }
}
