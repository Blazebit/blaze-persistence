package com.blazebit.persistence.impl.function.count;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.TemplateRenderer;

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
        int argsSize = context.getArgumentsSize();
        String maybeDistinct = context.getArgument(0);

        if (("'" + DISTINCT_QUALIFIER + "'").equalsIgnoreCase(maybeDistinct)) {
            distinct = true;
            startIndex++;
        }

        if (startIndex >= argsSize) {
            throw new RuntimeException("The " + AbstractCountFunction.FUNCTION_NAME + " function needs at least one expression to count! args=" + context);
        }

        return new Count(distinct, startIndex);
    }

    protected static final class Count {

        private final boolean distinct;
        private final int argumentStartIndex;

        public Count(boolean distinct, int argumentStartIndex) {
            this.distinct = distinct;
            this.argumentStartIndex = argumentStartIndex;
        }

        public boolean isDistinct() {
            return distinct;
        }

        public int getArgumentStartIndex() {
            return argumentStartIndex;
        }
    }
}
