/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.impl.function.cast;

import com.blazebit.persistence.impl.util.JpqlFunctionUtil;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CastFunction implements JpqlFunction {

    private final String functionName;
    private final Class<?> castType;
    private final String defaultSqlCastType;

    public CastFunction(Class<?> castType, DbmsDialect dbmsDialect) {
        this.functionName = "CAST_" + castType.getSimpleName().toUpperCase();
        this.castType = castType;
        this.defaultSqlCastType = dbmsDialect.getSqlType(castType);
    }

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
        return castType;
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() != 1 && context.getArgumentsSize() != 2) {
            throw new RuntimeException("The " + functionName + " function needs one argument <expression> with an optional second argument <sql-type-name>! args=" + context);
        }

        context.addChunk("cast(");
        context.addArgument(0);
        context.addChunk(" as ");
        if (context.getArgumentsSize() == 1) {
            context.addChunk(defaultSqlCastType);
        } else {
            context.addChunk(JpqlFunctionUtil.unquote(context.getArgument(1)));
        }
        context.addChunk(")");
    }

}
