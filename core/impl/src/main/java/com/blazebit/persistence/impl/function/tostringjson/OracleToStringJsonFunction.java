/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.tostringjson;

import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * Oracle Database 12c Release 2 included support for JSON_OBJECT and JSON_ARRAYAGG
 * <p>
 * Adapted from MySQLToStringJsonFunction by Christian Beikov
 *
 * @author Christian Beikov
 * @author Michael Saull
 * @since 1.6.19
 */
public class OracleToStringJsonFunction extends AbstractToStringJsonFunction {

    private static final String START_CHUNK = "(select json_arrayagg(json_object('";
    private static final String ELEMENT_POST_CHUNK = ")";
    private static final String AGGREGATE_POST_CHUNK = "RETURNING CLOB)"; //To avoid VARCHAR2(4000) limit
    private static final String POST_CHUNK = ELEMENT_POST_CHUNK + AGGREGATE_POST_CHUNK;

    @Override
    public void render(FunctionRenderContext context, String[] fields, String[] selectItemExpressions, String subquery, int fromIndex) {
        context.addChunk(START_CHUNK);

        int orderByIndex = SqlUtils.indexOfOrderBy(subquery, fromIndex);
        if (orderByIndex == -1) {
            renderJsonObjectArguments(context, fields, selectItemExpressions);
            context.addChunk(POST_CHUNK);
            context.addChunk(subquery.substring(fromIndex));
        } else {
            int limitIndex = SqlUtils.indexOfLimit(subquery, orderByIndex);
            if (limitIndex == -1) {
                renderJsonObjectArguments(context, fields, selectItemExpressions);
                context.addChunk(POST_CHUNK);
                context.addChunk(" OVER (");
                context.addChunk(subquery.substring(orderByIndex));
                context.addChunk(")");
                context.addChunk(subquery.substring(fromIndex, orderByIndex));
            } else {
                renderJsonObjectArguments(context, fields, fields); //I am not sure if this part is correct limit syntax for oracle? Might need to use the lateral join from OracleToStringJsonFunction (V1)
                context.addChunk(POST_CHUNK);
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
        writeFieldItemPair(context, fields[0], selectItemExpressions[0]);
        for (int i = 1; i < fields.length; i++) { //abstract ensures size
            context.addChunk(",'");
            writeFieldItemPair(context, fields[i], selectItemExpressions[i]);
        }
    }

    private void writeFieldItemPair(FunctionRenderContext context, String field, String itemExpression) {
        boolean subJson = itemExpression.startsWith(START_CHUNK);

        context.addChunk(field);

        if(subJson){ //treat as JSON to prevent " escaping
            context.addChunk("':");
            context.addChunk(itemExpression);
            context.addChunk("FORMAT JSON");
        }else { //cast to string
            context.addChunk("':TO_CHAR(");
            context.addChunk(itemExpression);
            context.addChunk(")");
        }
    }

}