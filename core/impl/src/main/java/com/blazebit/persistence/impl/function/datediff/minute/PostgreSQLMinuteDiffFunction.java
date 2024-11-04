/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.minute;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class PostgreSQLMinuteDiffFunction extends MinuteDiffFunction {

    public PostgreSQLMinuteDiffFunction() {
        super("-cast(trunc(date_part('epoch', cast(?1 as timestamp) - cast(?2 as timestamp))/60) as integer)");
    }

    @Override
    protected void renderDiff(FunctionRenderContext context) {
        renderer.start(context).addArgument(0).addArgument(1).build();
    }
}
