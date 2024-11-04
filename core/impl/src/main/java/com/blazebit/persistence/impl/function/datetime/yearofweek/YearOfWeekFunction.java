/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.yearofweek;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.TemplateRenderer;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class YearOfWeekFunction implements JpqlFunction {

    protected final TemplateRenderer renderer;

    public YearOfWeekFunction() {
        this("EXTRACT(YEAR FROM DATE_TRUNC('week', ?1))");
    }

    public YearOfWeekFunction(String template) {
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
            throw new RuntimeException("The second function needs exactly one argument <datetime>! args=" + context);
        }

        renderDiff(context);
    }

    protected void renderDiff(FunctionRenderContext context) {
        renderer.start(context).addArgument(0).build();
    }
}
