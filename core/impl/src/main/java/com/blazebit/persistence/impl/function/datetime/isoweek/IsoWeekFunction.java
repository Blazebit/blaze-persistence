/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.isoweek;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.TemplateRenderer;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class IsoWeekFunction implements JpqlFunction {

    private final TemplateRenderer renderer;

    public IsoWeekFunction() {
        this.renderer = new TemplateRenderer("extract(week from ?1)");
    }

    public IsoWeekFunction(String template) {
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
            throw new RuntimeException("The year function needs exactly one argument <datetime>! args=" + context);
        }

        renderer.start(context).addArgument(0).build();
    }
}
