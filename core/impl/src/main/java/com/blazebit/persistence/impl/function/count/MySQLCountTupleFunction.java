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
public class MySQLCountTupleFunction extends AbstractCountFunction {

    private static final String DISTINCT = "distinct ";

    @Override
    public void render(FunctionRenderContext context) {
        Count count = getCount(context);

        context.addChunk("count(");

        if (count.isDistinct()) {
            context.addChunk(DISTINCT);
        }

        List<String> args = count.getArguments();
        int size = args.size();
        if (size > 1) {
            if (count.isDistinct()) {
                context.addChunk(args.get(0));
                for (int i = 1; i < size; i++) {
                    context.addChunk(", ");
                    context.addChunk(args.get(i));
                }
            } else {
                context.addChunk("case when ");
                context.addChunk(args.get(0));
                context.addChunk(" is null");
                for (int i = 1; i < size; i++) {
                    context.addChunk(" or ");
                    context.addChunk(args.get(i));
                    context.addChunk(" is null");
                }
                context.addChunk(" then null else 1 end");
            }
        } else {
            context.addChunk(args.get(0));
        }

        context.addChunk(")");
    }
}
