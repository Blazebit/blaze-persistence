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
public class DB2JsonSetFunction extends AbstractJsonSetFunction {

    @Override
    protected void render0(FunctionRenderContext context) {
        context.addChunk("json_query(SYSTOOLS.BSON2JSON(SYSTOOLS.JSON_UPDATE(SYSTOOLS.JSON2BSON(");
        context.addChunk("concat('{\"val\":', concat(");
        context.addArgument(0);
        context.addChunk(", '}'))");
        context.addChunk("),concat('{ $set: {\"");

        context.addChunk("val");
        for (int i = 2; i < context.getArgumentsSize(); i++) {
            context.addChunk(".");
            addUnquotedArgument(context, i);
        }

        context.addChunk("\": ',concat(");
        context.addArgument(1);
        context.addChunk(",'}}')))), '$.val')");
    }
}
