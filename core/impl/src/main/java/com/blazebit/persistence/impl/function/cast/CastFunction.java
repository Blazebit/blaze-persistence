/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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

    protected final String functionName;
    protected final Class<?> castType;
    protected final String defaultSqlCastType;

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
            context.addChunk(JpqlFunctionUtil.unquoteSingleQuotes(context.getArgument(1)));
        }
        context.addChunk(")");
    }

    public String getCastExpression(String argument) {
        return "cast(" + argument + " as " + defaultSqlCastType + ")";
    }

    public String startCastExpression() {
        return "cast(";
    }

    public String endCastExpression(String castType) {
        return " as " + (castType == null ? defaultSqlCastType : castType) + ")";
    }

}
