/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.impl.function.jsonset;

import com.blazebit.persistence.impl.util.JpqlFunctionUtil;
import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public abstract class AbstractJsonSetFunction extends AbstractJsonFunction {

    public static final String FUNCTION_NAME = "JSON_SET";

    protected AbstractJsonSetFunction() {
        super(null);
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
