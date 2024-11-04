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
public class DefaultMinuteDiffFunction extends MinuteDiffFunction {

    public DefaultMinuteDiffFunction() {
        super("datediff(mi, ?1, ?2)");
    }

    @Override
    protected void renderDiff(FunctionRenderContext context) {
        renderer.start(context).addArgument(0).addArgument(1).build();
    }
}
