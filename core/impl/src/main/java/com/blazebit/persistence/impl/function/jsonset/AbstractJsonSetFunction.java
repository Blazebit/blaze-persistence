/*
 * Copyright 2014 - 2021 Blazebit.
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
package com.blazebit.persistence.impl.function.jsonset;

import com.blazebit.persistence.impl.util.JpqlFunctionUtil;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public abstract class AbstractJsonSetFunction implements JpqlFunction {

    public static final String FUNCTION_NAME = "JSON_SET";

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
        return String.class;
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() < 3) {
            throw new RuntimeException("The " + FUNCTION_NAME + " function requires at least 3 arguments <jsonField>, <newValue>, <key1|arrayIndex1>, ..., <keyN|arrayIndexN>! args=" + context);
        }
        render0(context);
    }

    protected abstract void render0(FunctionRenderContext context);

    protected void addUnquotedArgument(FunctionRenderContext context, int argIndex) {
        context.addChunk(JpqlFunctionUtil.unquoteSingleQuotes(context.getArgument(argIndex)));
    }
}
