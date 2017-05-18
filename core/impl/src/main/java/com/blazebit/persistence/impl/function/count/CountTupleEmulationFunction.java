/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.impl.function.count;

import com.blazebit.persistence.spi.FunctionRenderContext;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CountTupleEmulationFunction extends AbstractCountFunction {

    private static final String DISTINCT = "distinct ";
    private static final boolean ANSI_SQL = true;

    private final String concatOperator;
    private final String castType;

    public CountTupleEmulationFunction() {
        this("||", null);
    }

    public CountTupleEmulationFunction(String concatOperator, String castType) {
        this.concatOperator = concatOperator;
        this.castType = castType;
    }

    @Override
    public void render(FunctionRenderContext context) {
        Count count = getCount(context);

        context.addChunk("count(");

        if (count.isDistinct()) {
            context.addChunk(DISTINCT);
        }

        List<String> args = count.getArguments();
        int size = args.size();
        if (size > 1) {
            // see https://hibernate.atlassian.net/browse/HHH-11042 for the workaround description
            if (ANSI_SQL) {
                if (count.isDistinct()) {
                    // NULL -> \0
                    // '' -> \0 + argumentNumber
                    //count(distinct coalesce(nullif(coalesce(col1 || '', '\0'), ''), '\01') || '\0' || coalesce(nullif(coalesce(col2 || '', '\0'), ''), '\02'))
                    context.addChunk("coalesce(nullif(coalesce(");
                    renderArgument(context, args.get(0));
                    int argumentNumber = 1;
                    for (int i = 1; i < size; i++, argumentNumber++) {
                        // Concat with empty string to get implicit conversion
                        context.addChunk(" ");
                        context.addChunk(concatOperator);
                        context.addChunk(" ''");
                        context.addChunk(", '\\0'), ''), '\\0");
                        context.addChunk(Integer.toString(argumentNumber));
                        context.addChunk("') ");
                        context.addChunk(concatOperator);
                        context.addChunk(" '\\0' ");
                        context.addChunk(concatOperator);
                        context.addChunk(" coalesce(nullif(coalesce(");
                        renderArgument(context, args.get(i));
                    }
                    // Concat with empty string to get implicit conversion
                    context.addChunk(" ");
                    context.addChunk(concatOperator);
                    context.addChunk(" ''");
                    context.addChunk(", '\\0'), ''), '\\0");
                    context.addChunk(Integer.toString(argumentNumber));
                    context.addChunk("')");
                } else {
                    context.addChunk("case when ");
                    context.addChunk(args.get(0));
                    context.addChunk(" is null");
                    for (int i = 1; i < size; i++) {
                        context.addChunk(" or ");
                        context.addChunk(args.get(i));
                        context.addChunk(" is null");
                    }
                    context.addChunk(" then null else 1 end");
                }
            } else {
                //count(distinct case when col1 is null or col2 is null then null else col1 || '\0' || col2 end) + count(case when col1 is null or col2 is null then 1 end)
                context.addChunk("case when ");
                context.addChunk(args.get(0));
                context.addChunk(" is null");
                for (int i = 1; i < size; i++) {
                    context.addChunk(" or ");
                    context.addChunk(args.get(i));
                    context.addChunk(" is null");
                }
                context.addChunk(" then null else ");

                renderArgument(context, args.get(0));
                for (int i = 1; i < size; i++) {
                    context.addChunk(" ");
                    context.addChunk(concatOperator);
                    context.addChunk(" '\\0' ");
                    context.addChunk(concatOperator);
                    context.addChunk(" ");
                    renderArgument(context, args.get(i));
                }
                context.addChunk(" end");

                // Count nulls
                context.addChunk(") + count(case when ");
                context.addChunk(args.get(0));
                context.addChunk(" is null");
                for (int i = 1; i < size; i++) {
                    context.addChunk(" or ");
                    context.addChunk(args.get(i));
                    context.addChunk(" is null");
                }
                context.addChunk(" then 1 end");
            }
        } else {
            context.addChunk(count.getArguments().get(0));
        }

        context.addChunk(")");
    }

    private void renderArgument(FunctionRenderContext context, String s) {
        if (castType == null) {
            context.addChunk(s);
        } else {
            context.addChunk("cast(");
            context.addChunk(s);
            context.addChunk(" as ");
            context.addChunk(castType);
            context.addChunk(")");
        }
    }

}
