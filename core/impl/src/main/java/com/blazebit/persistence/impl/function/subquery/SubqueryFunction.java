/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.subquery;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 * This is an internal FUNCTION that is used to bypass the Hibernate parser for rendering subqueries as
 * aggregate function arguments.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class SubqueryFunction implements JpqlFunction {

    public static final String FUNCTION_NAME = "subquery";

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
        return firstArgumentType;
    }

    @Override
    public void render(FunctionRenderContext context) {
        context.addArgument(0);
    }
}
