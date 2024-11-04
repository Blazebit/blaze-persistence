/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.impl.function.jsonget;

import com.blazebit.persistence.impl.function.concat.ConcatFunction;
import com.blazebit.persistence.impl.function.jsonset.AbstractJsonFunction;
import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public abstract class AbstractJsonGetFunction extends AbstractJsonFunction {

    public static final String FUNCTION_NAME = "JSON_GET";

    protected AbstractJsonGetFunction(ConcatFunction concatFunction) {
        super(concatFunction);
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
        if (context.getArgumentsSize() < 2) {
            throw new RuntimeException("The " + FUNCTION_NAME + " function requires at least two arguments <jsonField>, <key1|arrayIndex1>, ..., <keyN|arrayIndexN>! args=" + context);
        }
        render0(context);
    }

    protected abstract void render0(FunctionRenderContext context);
}
