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
public class MySQLStringJsonAggFunction extends AbstractStringJsonAggFunction {

    @Override
    public void render(FunctionRenderContext context) {
        if ((context.getArgumentsSize() & 1) == 1) {
            throw new RuntimeException("The string_json_agg function needs an even amount of arguments <key1>, <value1>, ..., <keyN>, <valueN>! args=" + context);
        }
        context.addChunk("json_arrayagg(json_object(");
        context.addArgument(0);
        for (int i = 1; i < context.getArgumentsSize(); i++) {
            context.addChunk(",");
            if ((i & 1) == 1) {
                context.addChunk("cast(");
                context.addArgument(i);
                context.addChunk(" as char)");
            } else {
                context.addArgument(i);
            }
        }
        context.addChunk("))");
    }

}