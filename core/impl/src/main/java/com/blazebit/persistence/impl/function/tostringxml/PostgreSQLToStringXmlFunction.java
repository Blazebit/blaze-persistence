/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.tostringxml;

import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class PostgreSQLToStringXmlFunction extends AbstractToStringXmlFunction {

    private static final String START_CHUNK = "(select xmlagg(xmlelement(name e";

    @Override
    public void render(FunctionRenderContext context, String[] fields, String[] selectItemExpressions, String subquery, int fromIndex) {
        context.addChunk(START_CHUNK);

        int orderByIndex = SqlUtils.indexOfOrderBy(subquery, fromIndex);
        if (orderByIndex == -1) {
            renderXmlElementArguments(context, fields, selectItemExpressions);
            context.addChunk("))");
            context.addChunk(subquery.substring(fromIndex));
        } else {
            if (SqlUtils.indexOfLimit(subquery, orderByIndex) == -1 && SqlUtils.indexOfFetchFirst(subquery, orderByIndex) == -1) {
                renderXmlElementArguments(context, fields, selectItemExpressions);
                context.addChunk("))");
                context.addChunk(" OVER (");
                context.addChunk(subquery.substring(orderByIndex));
                context.addChunk(")");
                context.addChunk(subquery.substring(fromIndex, orderByIndex));
            } else {
                renderXmlElementArguments(context, fields, fields);
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

    private void renderXmlElementArguments(FunctionRenderContext context, String[] fields, String[] selectItemExpressions) {
        for (int i = 0; i < fields.length; i++) {
            context.addChunk(", xmlelement(name ");
            context.addChunk(fields[i]);
            context.addChunk(",");
            if (selectItemExpressions[i].startsWith(START_CHUNK)) {
                context.addChunk(selectItemExpressions[i]);
            } else {
                context.addChunk("'' || ");
                context.addChunk(selectItemExpressions[i]);
            }
            context.addChunk(")");
        }
    }

}