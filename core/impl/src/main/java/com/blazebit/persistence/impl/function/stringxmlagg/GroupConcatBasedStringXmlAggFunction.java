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

package com.blazebit.persistence.impl.function.stringxmlagg;

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
public class GroupConcatBasedStringXmlAggFunction extends AbstractStringXmlAggFunction {

    protected final ConcatFunction concatFunction;
    private final AbstractGroupConcatFunction groupConcatFunction;
    private final ReplaceFunction replaceFunction;

    public GroupConcatBasedStringXmlAggFunction(AbstractGroupConcatFunction groupConcatFunction, ReplaceFunction replaceFunction, ConcatFunction concatFunction) {
        this.groupConcatFunction = groupConcatFunction;
        this.concatFunction = concatFunction;
        this.replaceFunction = replaceFunction;
    }

    @Override
    public void render(FunctionRenderContext context) {
        if ((context.getArgumentsSize() & 1) == 1) {
            throw new RuntimeException("The string_xml_agg function needs an even amount of arguments <key1>, <value1>, ..., <keyN>, <valueN>! args=" + context);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(concatFunction.startConcat());
        sb.append("'<e>'");
        sb.append(concatFunction.concatSeparator());
        sb.append(coalesceStart());
        sb.append(concatFunction.startConcat());
        sb.append("'<'");
        sb.append(concatFunction.concatSeparator());
        sb.append(context.getArgument(0));
        for (int i = 1; i < context.getArgumentsSize(); i++) {
            if ((i & 1) == 1) {
                sb.append(concatFunction.concatSeparator());
                sb.append("'>'");
                sb.append(concatFunction.concatSeparator());
                sb.append(escape(context.getArgument(i)));
                sb.append(concatFunction.concatSeparator());
                sb.append("'</'");
                sb.append(concatFunction.concatSeparator());
                sb.append(context.getArgument(i - 1));
                sb.append(concatFunction.concatSeparator());
                sb.append("'>'");
                sb.append(concatFunction.endConcat());
                sb.append(coalesceEnd(context.getArgument(i - 1)));
            } else {
                sb.append(concatFunction.concatSeparator());
                sb.append(coalesceStart());
                sb.append(concatFunction.startConcat());
                sb.append("'<'");
                sb.append(concatFunction.concatSeparator());
                sb.append(context.getArgument(i));
            }
        }
        sb.append(concatFunction.concatSeparator());
        sb.append("'</e>'");
        sb.append(concatFunction.endConcat());

        groupConcatFunction.render(context, new AbstractGroupConcatFunction.GroupConcat(false, sb.toString(), Collections.<Order>emptyList(), ","));
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