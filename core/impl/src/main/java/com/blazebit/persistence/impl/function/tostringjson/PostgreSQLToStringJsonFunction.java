/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.tostringjson;

import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class PostgreSQLToStringJsonFunction extends AbstractToStringJsonFunction {

    private static final String START_CHUNK = "(select jsonb_agg(jsonb_build_object('";

    @Override
    public void render(FunctionRenderContext context, String[] fields, String[] selectItemExpressions, String subquery, int fromIndex) {
        context.addChunk(START_CHUNK);

        int orderByIndex = SqlUtils.indexOfOrderBy(subquery, fromIndex);
        if (orderByIndex == -1) {
            renderJsonObjectArguments(context, fields, selectItemExpressions);
            context.addChunk("))");
            context.addChunk(subquery.substring(fromIndex));
        } else {
            int limitIndex = SqlUtils.indexOfLimit(subquery, orderByIndex);
            if (limitIndex == -1) {
                renderJsonObjectArguments(context, fields, selectItemExpressions);
                context.addChunk("))");
                context.addChunk(" OVER (");
                context.addChunk(subquery.substring(orderByIndex));
                context.addChunk(")");
                context.addChunk(subquery.substring(fromIndex, orderByIndex));
            } else {
                renderJsonObjectArguments(context, fields, fields);
                context.addChunk("))");
                context.addChunk(" from (select ");
                for (int i = 0; i < fields.length; i++) {
                    if (i != 0) {
                        context.addChunk(",");
                    }
                    context.addChunk(selectItemExpressions[i]);
                    context.addChunk(" ");
                    context.addChunk(fields[i]);
                }
                context.addChunk(subquery.substring(fromIndex));
                context.addChunk(" tmp)");
            }
        }
    }

    private void renderJsonObjectArguments(FunctionRenderContext context, String[] fields, String[] selectItemExpressions) {
        // PostgreSQL has a limit on the amount of function arguments, so we have to split for every 100 arguments
        for (int j = 0; j < fields.length; j += 50) {
            if (j != 0) {
                context.addChunk( ") || jsonb_build_object('" );
            }
            int end = Math.min(j + 50, fields.length);
            for (int i = j; i < end; i++) {
                if (i != j) {
                    context.addChunk(",'");
                }
                context.addChunk(fields[i]);
                context.addChunk("',");
                if (selectItemExpressions[i].startsWith(START_CHUNK)) {
                    context.addChunk(selectItemExpressions[i]);
                } else {
                    context.addChunk("'' || ");
                    context.addChunk(selectItemExpressions[i]);
                }
            }
        }
    }

}