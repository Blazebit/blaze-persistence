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

package com.blazebit.persistence.impl.function.tostringxml;

import com.blazebit.persistence.impl.function.Order;
import com.blazebit.persistence.impl.function.concat.ConcatFunction;
import com.blazebit.persistence.impl.function.groupconcat.AbstractGroupConcatFunction;
import com.blazebit.persistence.impl.function.replace.ReplaceFunction;
import com.blazebit.persistence.spi.FunctionRenderContext;

import java.util.Collections;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class GroupConcatBasedToStringXmlFunction extends AbstractToStringXmlFunction {

    private final AbstractGroupConcatFunction groupConcatFunction;
    private final ConcatFunction concatFunction;
    private final ReplaceFunction replaceFunction;

    public GroupConcatBasedToStringXmlFunction(AbstractGroupConcatFunction groupConcatFunction, ReplaceFunction replaceFunction, ConcatFunction concatFunction) {
        this.groupConcatFunction = groupConcatFunction;
        this.concatFunction = concatFunction;
        this.replaceFunction = replaceFunction;
    }

    @Override
    public void render(FunctionRenderContext context, String[] fields, String[] selectItemExpressions, String subquery, int fromIndex) {
        context.addChunk("(select ");

        StringBuilder sb = new StringBuilder(fromIndex);
        sb.append(concatFunction.startConcat());
        sb.append("'<e>'");
        sb.append(concatFunction.concatSeparator());
        for (int i = 0; i < fields.length; i++) {
            if (i == 0) {
                sb.append(coalesceStart());
                sb.append(concatFunction.startConcat());
                sb.append("'<");
            } else {
                sb.append(concatFunction.concatSeparator());
                sb.append(coalesceStart());
                sb.append(concatFunction.startConcat());
                sb.append("'<");
            }
            sb.append(fields[i]);
            sb.append(">'");
            sb.append(concatFunction.concatSeparator());
            sb.append(escape(selectItemExpressions[i]));
            sb.append(concatFunction.concatSeparator());
            sb.append("'</");
            sb.append(fields[i]);
            sb.append(">'");
            sb.append(concatFunction.endConcat());
            sb.append(coalesceEnd(fields[i]));
        }
        sb.append(concatFunction.concatSeparator());
        sb.append("'</e>'");
        sb.append(concatFunction.endConcat());

        groupConcatFunction.render(context, new AbstractGroupConcatFunction.GroupConcat(false, sb.toString(), Collections.<Order>emptyList(), ","));

        context.addChunk(subquery.substring(fromIndex));
    }

    protected String coalesceStart() {
        return "coalesce(";
    }

    protected String coalesceEnd(String field) {
        return ",'')";
    }

    private String escape(String argument) {
        argument = replaceFunction.getReplaceString(argument, "'&'", "'&amp;'");
        argument = replaceFunction.getReplaceString(argument, "'<'", "'&lt;'");
        argument = replaceFunction.getReplaceString(argument, "'>'", "'&gt;'");
        return argument;
    }
}