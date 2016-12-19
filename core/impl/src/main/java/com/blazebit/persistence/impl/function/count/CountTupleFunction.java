/*
 * Copyright 2014 - 2016 Blazebit.
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

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class CountTupleFunction extends AbstractCountFunction {

    private static final String COUNT = "count(";
    private static final String DISTINCT = "distinct ";
    private static final String COUNT_DISTINCT = COUNT + DISTINCT;

    @Override
    public void render(FunctionRenderContext context) {
        Count count = getCount(context);

        if (count.isDistinct()) {
            context.addChunk(COUNT_DISTINCT);
        } else {
            context.addChunk(COUNT);
        }

        int argumentStartIndex = count.getArgumentStartIndex();

        if (count.getCountArgumentSize() > 1) {
            context.addChunk("(");
            context.addArgument(argumentStartIndex);
            for (int i = argumentStartIndex + 1; i < context.getArgumentsSize(); i++) {
                context.addChunk(", ");
                context.addArgument(i);
            }
            context.addChunk(")");
        } else {
            context.addArgument(argumentStartIndex);
        }

        context.addChunk(")");
    }
}
