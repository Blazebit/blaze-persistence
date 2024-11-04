/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.hour;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class DB2HourDiffFunction extends HourDiffFunction {

    public DB2HourDiffFunction() {
        // NOTE: we need lateral, otherwise the alias will be lost in the subquery
        super("(select (days(t2) - days(t1)) * 24 + (midnight_seconds(t2) - midnight_seconds(t1)) / " + (60 * 60) + " from lateral(values (cast(?1 as timestamp), cast(?2 as timestamp))) as temp(t1,t2))");
    }

    @Override
    protected void renderDiff(FunctionRenderContext context) {
        renderer.start(context).addArgument(0).addArgument(1).build();
    }
}
