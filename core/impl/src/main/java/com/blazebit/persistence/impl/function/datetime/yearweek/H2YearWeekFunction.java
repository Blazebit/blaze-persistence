/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.yearweek;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class H2YearWeekFunction extends YearWeekFunction {

    public H2YearWeekFunction() {
        super("CONCAT(extract(year from DATE_TRUNC('week', ?1)), '-', iso_week(?1))");
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() == 0 || context.getArgument(0).contains("?")) {
            throw new RuntimeException("The second function does not support parameterized arguments for H2! args=" + context);
        }
        super.render(context);
    }
}
