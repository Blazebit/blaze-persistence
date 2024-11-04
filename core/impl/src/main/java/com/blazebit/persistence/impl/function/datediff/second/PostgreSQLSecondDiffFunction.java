/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.second;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class PostgreSQLSecondDiffFunction extends SecondDiffFunction {

    public PostgreSQLSecondDiffFunction() {
        super("-cast(trunc(EXTRACT(EPOCH FROM cast(?1 as timestamp))) - trunc(EXTRACT(EPOCH FROM cast(?2 as timestamp))) as bigint)");
    }

    @Override
    protected void renderDiff(FunctionRenderContext context) {
        renderer.start(context).addArgument(0).addArgument(1).build();
    }
}
