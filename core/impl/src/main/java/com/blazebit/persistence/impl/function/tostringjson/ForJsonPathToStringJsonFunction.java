/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.tostringjson;

import com.blazebit.persistence.impl.function.cast.CastFunction;
import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class ForJsonPathToStringJsonFunction extends AbstractToStringJsonFunction {

    private static final String END_CHUNK = " for json path)";
    private final CastFunction castFunction;

    public ForJsonPathToStringJsonFunction(CastFunction castFunction) {
        this.castFunction = castFunction;
    }

    @Override
    public void render(FunctionRenderContext context, String[] fields, String[] selectItemExpressions, String subquery, int fromIndex) {
        context.addChunk("(select ");

        for (int i = 0; i < fields.length; i++) {
            if (i != 0) {
                context.addChunk(",");
            }
            if (selectItemExpressions[i].endsWith(END_CHUNK)) {
                context.addChunk(selectItemExpressions[i]);
            } else {
                context.addChunk(castFunction.getCastExpression(selectItemExpressions[i]));
            }
            context.addChunk(" as ");
            context.addChunk(fields[i]);
        }

        context.addChunk(subquery.substring(fromIndex, subquery.lastIndexOf(')')));
        context.addChunk(" for json path)");
    }
}