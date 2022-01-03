/*
 * Copyright 2014 - 2022 Blazebit.
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
import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.LateralStyle;

import java.util.Collections;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class GroupConcatBasedToStringXmlFunction extends AbstractToStringXmlFunction {

    private final AbstractGroupConcatFunction groupConcatFunction;
    private final ConcatFunction concatFunction;
    private final ReplaceFunction replaceFunction;
    private final LateralStyle lateralStyle;

    public GroupConcatBasedToStringXmlFunction(AbstractGroupConcatFunction groupConcatFunction, ReplaceFunction replaceFunction, ConcatFunction concatFunction, LateralStyle lateralStyle) {
        this.groupConcatFunction = groupConcatFunction;
        this.concatFunction = concatFunction;
        this.replaceFunction = replaceFunction;
        this.lateralStyle = lateralStyle;
    }

    @Override
    public void render(FunctionRenderContext context, String[] fields, String[] selectItemExpressions, String subquery, int fromIndex) {
        context.addChunk("(select ");

        int orderByIndex = SqlUtils.indexOfOrderBy(subquery, fromIndex);
        if (orderByIndex == -1) {
            groupConcatFunction.render(context, new AbstractGroupConcatFunction.GroupConcat(false, createGroupConcatArgument(fields, selectItemExpressions, fromIndex), Collections.<Order>emptyList(), ","));
            context.addChunk(subquery.substring(fromIndex));
        } else {
            int limitIndex = SqlUtils.indexOfLimit(subquery, orderByIndex);
            if (limitIndex == -1) {
                groupConcatFunction.render(context, new AbstractGroupConcatFunction.GroupConcat(false, createGroupConcatArgument(fields, selectItemExpressions, fromIndex), Collections.<Order>emptyList(), ","));
                context.addChunk(" OVER (");
                context.addChunk(subquery.substring(orderByIndex));
                context.addChunk(subquery.substring(fromIndex, orderByIndex));
            } else {
                if (lateralStyle == LateralStyle.NONE) {
                    groupConcatFunction.render(context, new AbstractGroupConcatFunction.GroupConcat(false, createGroupConcatArgument(fields, selectItemExpressions, fromIndex), Collections.<Order>emptyList(), ","));
                    context.addChunk(" OVER (");
                    context.addChunk(subquery.substring(orderByIndex, limitIndex));
                    String limitClause = subquery.substring(limitIndex + " limit ".length(), subquery.length() - 1);
                    int offsetIndex = limitClause.indexOf(" offset ");
                    if (offsetIndex == -1) {
                        context.addChunk(" ROWS BETWEEN CURRENT ROW AND (");
                        context.addChunk(limitClause);
                        context.addChunk(" - 1) FOLLOWING");
                    } else {
                        context.addChunk(" ROWS BETWEEN ");
                        context.addChunk(limitClause.substring(offsetIndex + " offset ".length()));
                        context.addChunk(" FOLLOWING AND ");
                        context.addChunk(limitClause.substring(0, offsetIndex));
                        context.addChunk(" FOLLOWING");
                    }
                    context.addChunk(")");
                    context.addChunk(subquery.substring(fromIndex, limitIndex));
                    context.addChunk(" limit 1)");
                } else {
                    groupConcatFunction.render(context, new AbstractGroupConcatFunction.GroupConcat(false, createGroupConcatArgument(fields, fields, fromIndex), Collections.<Order>emptyList(), ","));
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
    }

    private String createGroupConcatArgument(String[] fields, String[] selectItemExpressions, int fromIndex) {
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
        return sb.toString();
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