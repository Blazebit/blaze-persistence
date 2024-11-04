/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.stringjsonagg;

import com.blazebit.persistence.impl.function.chr.ChrFunction;
import com.blazebit.persistence.impl.function.concat.ConcatFunction;
import com.blazebit.persistence.impl.function.groupconcat.AbstractGroupConcatFunction;
import com.blazebit.persistence.impl.function.replace.ReplaceFunction;
import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class OracleStringJsonAggFunction extends GroupConcatBasedStringJsonAggFunction {

    public OracleStringJsonAggFunction(AbstractGroupConcatFunction groupConcatFunction, ChrFunction chrFunction, ReplaceFunction replaceFunction, ConcatFunction concatFunction) {
        super(groupConcatFunction, chrFunction, replaceFunction, concatFunction);
    }

    @Override
    public void render(FunctionRenderContext context) {
        if ((context.getArgumentsSize() & 1) == 1) {
            throw new RuntimeException("The string_json_agg function needs an even amount of arguments <key1>, <value1>, ..., <keyN>, <valueN>! args=" + context);
        }
        context.addChunk("('[' || dbms_xmlgen.convert(substr(xmlagg(xmlelement(e,to_clob(',')||");

        StringBuilder sb = new StringBuilder();
        render(sb, context);
        context.addChunk(sb.toString());
        context.addChunk(").extract('//text()')).getClobVal(),2),1) || ']')");
    }

    @Override
    protected String coalesceStart() {
        return "coalesce(nullif(";
    }

    @Override
    protected String coalesceEnd() {
        return ",'\"\"'),'null')";
    }
}