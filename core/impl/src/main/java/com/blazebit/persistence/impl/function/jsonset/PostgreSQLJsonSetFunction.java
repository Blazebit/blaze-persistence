/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.impl.function.jsonset;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class PostgreSQLJsonSetFunction extends AbstractJsonSetFunction {

    @Override
    protected void render0(FunctionRenderContext context) {
        context.addChunk("jsonb_set(cast(");
        context.addArgument(0);
        context.addChunk(" as jsonb),");
        context.addChunk("'{");
        addUnquotedArgument(context, 2);
        for (int i = 3; i < context.getArgumentsSize(); i++) {
            context.addChunk(",");
            addUnquotedArgument(context, i);
        }
        context.addChunk("}',");
        context.addArgument(1);
        context.addChunk(")");
    }
}
