/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.month;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class PostgreSQLMonthDiffFunction extends MonthDiffFunction {

    public PostgreSQLMonthDiffFunction() {
        super("(select cast(trunc((date_part('year', cast(t2 as timestamp)) - date_part('year', cast(t1 as timestamp))) * 12) + trunc(date_part('month', cast(t2 as timestamp)) - date_part('month', cast(t1 as timestamp))) as integer) from (values (?1,?2)) as temp(t1,t2))");
    }

    @Override
    protected void renderDiff(FunctionRenderContext context) {
        renderer.start(context).addArgument(0).addArgument(1).build();
    }
}
