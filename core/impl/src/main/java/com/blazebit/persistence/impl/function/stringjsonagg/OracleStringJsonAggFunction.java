/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.stringjsonagg;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * Oracle Database 12c Release 2 included support for JSON_OBJECT and JSON_ARRAYAGG
 *
 * @author Christian Beikov
 * @author Michael Saull
 * @since 1.6.19
 */
public class OracleStringJsonAggFunction extends AbstractStringJsonAggFunction {

    @Override
    public void render(FunctionRenderContext context) {
        int argumentsCount = context.getArgumentsSize();
        if ((argumentsCount & 1) == 1) {
            throw new RuntimeException("The string_json_agg function needs an even amount of arguments <key1>, <value1>, ..., <keyN>, <valueN>! args=" + context);
        }

        context.addChunk("json_arrayagg(json_object(");
        writeArgumentPair(context, 0);
        for (int i = 2; i < argumentsCount; i += 2) {
            context.addChunk(",");
            writeArgumentPair(context, i);
        }
        context.addChunk("))");
    }

    private static void writeArgumentPair(FunctionRenderContext context, int i) {
        context.addArgument(i);
         context.addChunk(" VALUE TO_CHAR(");
        context.addArgument(i + 1);
        context.addChunk(")");
    }
}