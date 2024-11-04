/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.year;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class PostgreSQLYearDiffFunction extends YearDiffFunction {

    public PostgreSQLYearDiffFunction() {
        super("-cast(trunc(date_part('year',cast(?1 as timestamp))) - trunc(date_part('year',cast(?2 as timestamp))) as integer)");
    }

    @Override
    protected void renderDiff(FunctionRenderContext context) {
        renderer.start(context).addArgument(0).addArgument(1).build();
    }
}
