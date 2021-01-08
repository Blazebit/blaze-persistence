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

package com.blazebit.persistence.impl.function.cast;

import com.blazebit.persistence.impl.util.JpqlFunctionUtil;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DB2CastFunction extends CastFunction {

    private static final String[] CLOB_RETURNING_FUNCTIONS = new String[] {
        "json_value(",
        "json_query("
    };
    private static final String[] CLOB_COMPATIBLE_CAST_TARGET_TYPES = new String[] {
        "char",
        "varchar",
        "graphic",
        "vargraphic",
        "dbclob",
        "blob",
        "xml"
    };

    public DB2CastFunction(Class<?> castType, DbmsDialect dbmsDialect) {
        super(castType, dbmsDialect);
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() != 1 && context.getArgumentsSize() != 2) {
            throw new RuntimeException("The " + functionName + " function needs one argument <expression> with an optional second argument <sql-type-name>! args=" + context);
        }
        String effectiveCastTargetType;
        if (context.getArgumentsSize() == 1) {
            effectiveCastTargetType = defaultSqlCastType;
        } else {
            effectiveCastTargetType = JpqlFunctionUtil.unquoteSingleQuotes(context.getArgument(1));
        }
        boolean insertVarcharCast = isClobReturningFunction(context.getArgument(0)) && !isClobCompatibleCastTarget(effectiveCastTargetType);
        context.addChunk("cast(");
        if (insertVarcharCast) {
            context.addChunk("cast(");
        }
        context.addArgument(0);
        if (insertVarcharCast) {
            context.addChunk(" as varchar(32000))");
        }
        context.addChunk(" as ");
        context.addChunk(effectiveCastTargetType);
        context.addChunk(")");
    }

    @Override
    public String getCastExpression(String argument) {
        boolean insertVarcharCast = isClobReturningFunction(argument) && !isClobCompatibleCastTarget(defaultSqlCastType);
        if (insertVarcharCast) {
            return "cast(cast(" + argument + " as varchar(32000)) as " + defaultSqlCastType + ")";
        } else {
            return "cast(" + argument + " as " + defaultSqlCastType + ")";
        }
    }

    private static boolean isClobReturningFunction(String castSource) {
        for (int i = 0; i < CLOB_RETURNING_FUNCTIONS.length; i++) {
            if (castSource.toLowerCase().startsWith(CLOB_RETURNING_FUNCTIONS[i])) {
                return true;
            }
        }
        return false;
    }

    private static boolean isClobCompatibleCastTarget(String castTargetType) {
        for (int i = 0; i < CLOB_COMPATIBLE_CAST_TARGET_TYPES.length; i++) {
            if (castTargetType.toLowerCase().indexOf(CLOB_COMPATIBLE_CAST_TARGET_TYPES[i]) == 0 &&
                    (castTargetType.length() == CLOB_COMPATIBLE_CAST_TARGET_TYPES[i].length() ||
                            castTargetType.charAt(CLOB_COMPATIBLE_CAST_TARGET_TYPES[i].length()) == '(')
            ) {
                return true;
            }
        }
        return false;
    }
}
