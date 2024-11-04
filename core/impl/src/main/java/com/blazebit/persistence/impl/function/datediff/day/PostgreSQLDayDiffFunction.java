/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.day;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class PostgreSQLDayDiffFunction extends DayDiffFunction {

    public PostgreSQLDayDiffFunction() {
        super("-cast(trunc(date_part('day', cast(?1 as timestamp) - cast(?2 as timestamp))) as integer)");
    }

    @Override
    protected void renderDiff(FunctionRenderContext context) {
        renderer.start(context).addArgument(0).addArgument(1).build();
    }
}