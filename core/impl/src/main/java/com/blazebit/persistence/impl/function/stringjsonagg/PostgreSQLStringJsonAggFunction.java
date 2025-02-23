/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.stringjsonagg;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class PostgreSQLStringJsonAggFunction extends AbstractStringJsonAggFunction {

    @Override
    public void render(FunctionRenderContext context) {
        int argumentsCount = context.getArgumentsSize();
        if ((argumentsCount & 1) == 1) {
            throw new RuntimeException("The string_json_agg function needs an even amount of arguments <key1>, <value1>, ..., <keyN>, <valueN>! args=" + context);
        }
        context.addChunk("jsonb_agg(");
        // PostgreSQL has a limit on the amount of function arguments, so we have to split for every 100 arguments
        for (int j = 0; j < argumentsCount; j += 100) {
            if (j != 0) {
                context.addChunk( " || " );
            }
            context.addChunk("jsonb_build_object(");
            context.addArgument(j);
            int end = Math.min(j + 100, argumentsCount);
            for (int i = j + 1; i < end; i++) {
                context.addChunk(",");
                if ((i & 1) == 1) {
                    context.addChunk("'' || ");
                    context.addArgument(i);
                } else {
                    context.addArgument(i);
                }
            }
            context.addChunk(")");
        }
        context.addChunk(")");
    }

}