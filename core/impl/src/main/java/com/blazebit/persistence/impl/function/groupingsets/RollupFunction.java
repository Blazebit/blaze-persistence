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

package com.blazebit.persistence.impl.function.groupingsets;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
public class RollupFunction implements JpqlFunction {

    public static final String FUNCTION_NAME = "rollup";

    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return true;
    }

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return Integer.class;
    }

    @Override
    public void render(FunctionRenderContext functionRenderContext) {
        int argumentsSize = functionRenderContext.getArgumentsSize();
        functionRenderContext.addChunk("rollup (");
        if (argumentsSize != 0) {
            functionRenderContext.addArgument(0);
            for (int i = 1; i < argumentsSize; i++) {
                functionRenderContext.addChunk(",");
                functionRenderContext.addArgument(i);
            }
        }
        functionRenderContext.addChunk(")");
    }

}
