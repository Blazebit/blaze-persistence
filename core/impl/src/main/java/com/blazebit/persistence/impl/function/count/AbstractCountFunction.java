/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.count;

import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public abstract class AbstractCountFunction implements JpqlFunction {

    public static final String FUNCTION_NAME = "count_tuple";
    public static final String DISTINCT_QUALIFIER = "DISTINCT";

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
        return Long.class;
    }

    protected Count getCount(FunctionRenderContext context) {
        if (context.getArgumentsSize() == 0) {
            throw new RuntimeException("The " + FUNCTION_NAME + " function needs at least one argument!");
        }

        boolean distinct = false;
        int startIndex = 0;
        String maybeDistinct = context.getArgument(0);

        if (("'" + DISTINCT_QUALIFIER + "'").equalsIgnoreCase(maybeDistinct)) {
            distinct = true;
            startIndex++;
        }

        // Hibernate puts the columns for embeddables into a single string, so we have to count items
        List<String> expressions = new ArrayList<>(context.getArgumentsSize());
        for (int i = startIndex; i < context.getArgumentsSize(); i++) {
            expressions.addAll(SqlUtils.getExpressionItems(context.getArgument(i)));
        }

        if (expressions.isEmpty()) {
            throw new RuntimeException("The " + AbstractCountFunction.FUNCTION_NAME + " function needs at least one expression to count! args=" + context);
        }

        return new Count(distinct, expressions);
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    protected static final class Count {

        private final boolean distinct;
        private final List<String> arguments;

        public Count(boolean distinct, List<String> arguments) {
            this.distinct = distinct;
            this.arguments = arguments;
        }

        public boolean isDistinct() {
            return distinct;
        }

        public List<String> getArguments() {
            return arguments;
        }
    }
}
