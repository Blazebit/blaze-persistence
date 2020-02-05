/*
 * Copyright 2014 - 2020 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.impl.function.tostringjson;

import com.blazebit.persistence.impl.function.chr.ChrFunction;
import com.blazebit.persistence.impl.function.concat.ConcatFunction;
import com.blazebit.persistence.impl.function.groupconcat.AbstractGroupConcatFunction;
import com.blazebit.persistence.impl.function.replace.ReplaceFunction;
import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class OracleToStringJsonFunction extends GroupConcatBasedToStringJsonFunction {

    public OracleToStringJsonFunction(AbstractGroupConcatFunction groupConcatFunction, ChrFunction chrFunction, ReplaceFunction replaceFunction, ConcatFunction concatFunction) {
        super("(select ('[' || dbms_xmlgen.convert(substr(xmlagg(xmlelement(e,to_clob(',')||", ").extract('//text()')).getClobVal(),2),1) || ']')", false, groupConcatFunction, chrFunction, replaceFunction, concatFunction);
    }

    @Override
    public void render(FunctionRenderContext context, String[] fields, String[] selectItemExpressions, String subquery, int fromIndex) {
        context.addChunk(preChunk);

        StringBuilder sb = new StringBuilder(fromIndex);
        render(sb, fields, selectItemExpressions);

        context.addChunk(sb.toString());
        context.addChunk(postChunk);
        context.addChunk(subquery.substring(fromIndex));
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