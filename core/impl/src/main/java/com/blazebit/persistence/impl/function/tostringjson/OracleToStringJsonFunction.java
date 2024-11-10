/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.tostringjson;

import com.blazebit.persistence.impl.function.chr.ChrFunction;
import com.blazebit.persistence.impl.function.concat.ConcatFunction;
import com.blazebit.persistence.impl.function.groupconcat.AbstractGroupConcatFunction;
import com.blazebit.persistence.impl.function.replace.ReplaceFunction;
import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.LateralStyle;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class OracleToStringJsonFunction extends GroupConcatBasedToStringJsonFunction {

    private static final String ELEMENT_POST_CHUNK = ").extract('//text()'))";
    private static final String AGGREGATE_POST_CHUNK = ".getClobVal(),2),1)) || ']')";
    private static final String POST_CHUNK = ELEMENT_POST_CHUNK + AGGREGATE_POST_CHUNK;

    public OracleToStringJsonFunction(AbstractGroupConcatFunction groupConcatFunction, ChrFunction chrFunction, ReplaceFunction replaceFunction, ConcatFunction concatFunction) {
        super("(select ('[' || (dbms_xmlgen.convert(substr(xmlagg(xmlelement(e,to_clob(',')||", POST_CHUNK, false, groupConcatFunction, chrFunction, replaceFunction, concatFunction, LateralStyle.LATERAL);
    }

    @Override
    public void render(FunctionRenderContext context, String[] fields, String[] selectItemExpressions, String subquery, int fromIndex) {
        int orderByIndex = SqlUtils.indexOfOrderBy(subquery, fromIndex);
        if (orderByIndex == -1) {
            context.addChunk(preChunk);
            StringBuilder sb = new StringBuilder(fromIndex);
            render(sb, fields, selectItemExpressions);
            context.addChunk(sb.toString());
            context.addChunk(postChunk);
            context.addChunk(subquery.substring(fromIndex));
        } else {
            if (SqlUtils.indexOfLimit(subquery, orderByIndex) == -1 && SqlUtils.indexOfFetchFirst(subquery, orderByIndex) == -1) {
                context.addChunk(preChunk);

                StringBuilder sb = new StringBuilder(fromIndex);
                render(sb, fields, selectItemExpressions);
                context.addChunk(sb.toString());

                context.addChunk(ELEMENT_POST_CHUNK);
                context.addChunk(" OVER (");
                context.addChunk(subquery.substring(orderByIndex));
                context.addChunk(")");
                context.addChunk(AGGREGATE_POST_CHUNK);
                context.addChunk(subquery.substring(fromIndex, orderByIndex));
                context.addChunk(")");
            } else {
                context.addChunk(preChunk);

                StringBuilder sb = new StringBuilder(fromIndex);
                render(sb, fields, selectItemExpressions);
                context.addChunk(sb.toString());

                context.addChunk(postChunk);
                context.addChunk(" from lateral(select ");
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

    @Override
    protected String coalesceStart() {
        return "coalesce(nullif(";
    }

    @Override
    protected String coalesceEnd(String field) {
        return ",'\"\"'),'null')";
    }
}