/*
 * Copyright 2014 - 2023 Blazebit.
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

import com.blazebit.persistence.impl.function.Order;
import com.blazebit.persistence.impl.function.chr.ChrFunction;
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
public class GroupConcatBasedToStringJsonFunction extends AbstractToStringJsonFunction {

    protected final ConcatFunction concatFunction;
    protected final String preChunk;
    protected final String postChunk;
    protected final String start;
    protected final String end;
    private final boolean coalesceNestedMultiset;
    private final AbstractGroupConcatFunction groupConcatFunction;
    private final LateralStyle lateralStyle;

    public GroupConcatBasedToStringJsonFunction(AbstractGroupConcatFunction groupConcatFunction, ChrFunction chrFunction, ReplaceFunction replaceFunction, ConcatFunction concatFunction, LateralStyle lateralStyle) {
        this(getPreChunk(concatFunction), getPostChunk(concatFunction), true, groupConcatFunction, chrFunction, replaceFunction, concatFunction, lateralStyle);
    }

    protected GroupConcatBasedToStringJsonFunction(String preChunk, String postChunk, boolean coalesceNestedMultiset, AbstractGroupConcatFunction groupConcatFunction, ChrFunction chrFunction, ReplaceFunction replaceFunction, ConcatFunction concatFunction, LateralStyle lateralStyle) {
        this.preChunk = preChunk;
        this.postChunk = postChunk;
        this.coalesceNestedMultiset = coalesceNestedMultiset;
        this.groupConcatFunction = groupConcatFunction;
        this.concatFunction = concatFunction;
        this.lateralStyle = lateralStyle;
        String backslash = chrFunction.getEncodedString(Integer.toString('\\'));
        String backslashB = chrFunction.getEncodedString(Integer.toString('\b'));
        String backslashF = chrFunction.getEncodedString(Integer.toString('\f'));
        String backslashN = chrFunction.getEncodedString(Integer.toString('\n'));
        String backslashR = chrFunction.getEncodedString(Integer.toString('\r'));
        String backslashT = chrFunction.getEncodedString(Integer.toString('\t'));
        String backslashBackslash = concatFunction.startConcat() + backslash + concatFunction.concatSeparator() + backslash + concatFunction.endConcat();
        String backslashBackslashB = concatFunction.startConcat() + backslash + concatFunction.concatSeparator() + chrFunction.getEncodedString(Integer.toString('\b')) + concatFunction.endConcat();
        String backslashBackslashF = concatFunction.startConcat() + backslash + concatFunction.concatSeparator() + chrFunction.getEncodedString(Integer.toString('\f')) + concatFunction.endConcat();
        String backslashBackslashN = concatFunction.startConcat() + backslash + concatFunction.concatSeparator() + chrFunction.getEncodedString(Integer.toString('\n')) + concatFunction.endConcat();
        String backslashBackslashR = concatFunction.startConcat() + backslash + concatFunction.concatSeparator() + chrFunction.getEncodedString(Integer.toString('\r')) + concatFunction.endConcat();
        String backslashBackslashT = concatFunction.startConcat() + backslash + concatFunction.concatSeparator() + chrFunction.getEncodedString(Integer.toString('\t')) + concatFunction.endConcat();
        String backslashSlash = concatFunction.startConcat() + backslash + concatFunction.concatSeparator() + chrFunction.getEncodedString(Integer.toString('/')) + concatFunction.endConcat();
        String backslashQuote = concatFunction.startConcat() + backslash + concatFunction.concatSeparator() + "'\"'" + concatFunction.endConcat();

        String argument = "\0";
        argument = replaceFunction.getReplaceString(argument, backslash, backslashBackslash);
        argument = replaceFunction.getReplaceString(argument, "'/'", backslashSlash);
        argument = replaceFunction.getReplaceString(argument, backslashB, backslashBackslashB);
        argument = replaceFunction.getReplaceString(argument, backslashF, backslashBackslashF);
        argument = replaceFunction.getReplaceString(argument, backslashN, backslashBackslashN);
        argument = replaceFunction.getReplaceString(argument, backslashR, backslashBackslashR);
        argument = replaceFunction.getReplaceString(argument, backslashT, backslashBackslashT);
        argument = replaceFunction.getReplaceString(argument, "'\"'", backslashQuote);
        String[] split = argument.split("\0");
        this.start = split[0];
        this.end = split[1];
    }

    private static String getPreChunk(ConcatFunction concatFunction) {
        return "(select " + concatFunction.startConcat() + "'['" + concatFunction.concatSeparator();
    }

    private static String getPostChunk(ConcatFunction concatFunction) {
        return concatFunction.concatSeparator() + "']'" + concatFunction.endConcat();
    }

    @Override
    public void render(FunctionRenderContext context, String[] fields, String[] selectItemExpressions, String subquery, int fromIndex) {
        int orderByIndex = SqlUtils.indexOfOrderBy(subquery, fromIndex);
        if (orderByIndex == -1) {
            context.addChunk(preChunk);
            groupConcatFunction.render(context, new AbstractGroupConcatFunction.GroupConcat(false, createGroupConcatArgument(fields, selectItemExpressions, fromIndex), Collections.<Order>emptyList(), ","));
            context.addChunk(postChunk);
            context.addChunk(subquery.substring(fromIndex));
        } else {
            String limitText = SqlUtils.LIMIT;
            int limitIndex = SqlUtils.indexOfLimit(subquery, orderByIndex);
            if (limitIndex == -1) {
                limitText = SqlUtils.FETCH_FIRST;
                limitIndex = SqlUtils.indexOfFetchFirst(subquery, orderByIndex);
            }
            if (limitIndex == -1) {
                context.addChunk(preChunk);
                groupConcatFunction.render(context, new AbstractGroupConcatFunction.GroupConcat(false, createGroupConcatArgument(fields, selectItemExpressions, fromIndex), Collections.<Order>emptyList(), ","));
                context.addChunk(" OVER (");
                context.addChunk(subquery.substring(orderByIndex));
                context.addChunk(postChunk);
                context.addChunk(subquery.substring(fromIndex, orderByIndex));
                context.addChunk(")");
            } else {
                if (lateralStyle == LateralStyle.NONE) {
                    context.addChunk(preChunk);
                    groupConcatFunction.render(context, new AbstractGroupConcatFunction.GroupConcat(false, createGroupConcatArgument(fields, selectItemExpressions, fromIndex), Collections.<Order>emptyList(), ","));
                    context.addChunk(" OVER (");
                    context.addChunk(subquery.substring(orderByIndex, limitIndex));
                    String limitClause = subquery.substring(limitIndex + limitText.length(), subquery.length() - 1);
                    String offsetText = " offset ";
                    int offsetIndex = limitClause.indexOf(offsetText);
                    if (offsetIndex == -1) {
                        context.addChunk(" ROWS BETWEEN CURRENT ROW AND (");
                        context.addChunk(limitClause);
                        context.addChunk(" - 1) FOLLOWING");
                    } else {
                        context.addChunk(" ROWS BETWEEN ");
                        context.addChunk(limitClause.substring(offsetIndex + offsetText.length()));
                        context.addChunk(" FOLLOWING AND ");
                        context.addChunk(limitClause.substring(0, offsetIndex));
                        context.addChunk(" FOLLOWING");
                    }
                    context.addChunk(")");
                    context.addChunk(postChunk);
                    context.addChunk(subquery.substring(fromIndex, limitIndex));
                    context.addChunk(" limit 1)");
                } else {
                    context.addChunk(preChunk);
                    groupConcatFunction.render(context, new AbstractGroupConcatFunction.GroupConcat(false, createGroupConcatArgument(fields, fields, fromIndex), Collections.<Order>emptyList(), ","));
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
    }

    private String createGroupConcatArgument(String[] fields, String[] selectItemExpressions, int fromIndex) {
        StringBuilder sb = new StringBuilder(fromIndex);
        render(sb, fields, selectItemExpressions);
        return sb.toString();
    }

    protected void render(StringBuilder sb, String[] fields, String[] selectItemExpressions) {
        sb.append(concatFunction.startConcat());
        sb.append("'{\"");
        for (int i = 0; i < fields.length; i++) {
            if (i != 0) {
                sb.append(concatFunction.concatSeparator());
                sb.append("',\"");
            }
            sb.append(fields[i]);
            sb.append("\":'");
            sb.append(concatFunction.concatSeparator());
            // Don't escape nested multiset invocations
            if (selectItemExpressions[i].startsWith(preChunk)) {
                if (coalesceNestedMultiset) {
                    sb.append(coalesceStart());
                    sb.append(selectItemExpressions[i]);
                    sb.append(coalesceEnd(fields[i]));
                } else {
                    sb.append(selectItemExpressions[i]);
                }
            } else {
                sb.append(coalesceStart());
                sb.append(concatFunction.startConcat());
                sb.append("'\"'");
                sb.append(concatFunction.concatSeparator());
                sb.append(start);
                sb.append(selectItemExpressions[i]);
                sb.append(end);
                sb.append(concatFunction.concatSeparator());
                sb.append("'\"'");
                sb.append(concatFunction.endConcat());
                sb.append(coalesceEnd(fields[i]));
            }
        }
        sb.append(concatFunction.concatSeparator());
        sb.append("'}'");
        sb.append(concatFunction.endConcat());
    }

    protected String coalesceStart() {
        return "coalesce(";
    }

    protected String coalesceEnd(String field) {
        return ",'null')";
    }
}