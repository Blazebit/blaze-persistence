/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.second;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.TemplateRenderer;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class MySQLSecondDiffFunction extends SecondDiffFunction {

    private final TemplateRenderer bothParamRenderer;

    public MySQLSecondDiffFunction() {
        super("truncate(unix_timestamp(?2), 0) - truncate(unix_timestamp(?1), 0)");
        this.bothParamRenderer = new TemplateRenderer("(select truncate(unix_timestamp(t2), 0) - truncate(unix_timestamp(t1), 0) from (select ?1 as t1, ?2 as t2) as temp)");
    }

    @Override
    protected void renderDiff(FunctionRenderContext context) {
        boolean firstContains = context.getArgument(0).contains("?");
        boolean secondContains = context.getArgument(1).contains("?");
        if (firstContains && context.getArgument(0).length() != 1
                || secondContains && context.getArgument(1).length() != 1) {
            throw new IllegalArgumentException("Only simple parameters or expressions are allowed because of a needed reordering in SQL which is otherwise not possible! Expressions ['" + context.getArgument(0) + "', '" + context.getArgument(1) + "'] do not comply!");
        }
        if (firstContains & secondContains) {
            bothParamRenderer.start(context).addArgument(0).addArgument(1).build();
        } else {
            // Reuse the expression multiple times
            renderer.start(context).addArgument(0).addArgument(1).build();
        }
    }
}
