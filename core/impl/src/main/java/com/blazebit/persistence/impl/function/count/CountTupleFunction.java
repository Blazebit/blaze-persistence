/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.count;

import com.blazebit.persistence.spi.FunctionRenderContext;

import java.util.List;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class CountTupleFunction extends AbstractCountFunction {

    private static final String COUNT = "count(";
    private static final String DISTINCT = "distinct ";
    private static final String COUNT_DISTINCT = COUNT + DISTINCT;

    @Override
    public void render(FunctionRenderContext context) {
        Count count = getCount(context);

        if (count.isDistinct()) {
            context.addChunk(COUNT_DISTINCT);
        } else {
            context.addChunk(COUNT);
        }

        List<String> args = count.getArguments();
        int size = args.size();
        if (size > 1) {
            context.addChunk("(");
            context.addChunk(args.get(0));
            for (int i = 1; i < size; i++) {
                context.addChunk(", ");
                context.addChunk(args.get(i));
            }
            context.addChunk(")");
        } else {
            context.addChunk(args.get(0));
        }

        context.addChunk(")");
    }
}
