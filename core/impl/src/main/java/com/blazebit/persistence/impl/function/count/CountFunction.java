package com.blazebit.persistence.impl.function.count;

import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.function.groupconcat.AbstractGroupConcatFunction;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.TemplateRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 16.09.2016.
 */
public class CountFunction implements JpqlFunction {

    public static final String FUNCTION_NAME = "count_tuple";
    public static final String DISTINCT_QUALIFIER = "DISTINCT";

    TemplateRenderer renderer = new TemplateRenderer("COUNT(?1)");

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

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() == 0) {
            throw new RuntimeException("The " + CountFunction.FUNCTION_NAME + " function needs at least one argument!");
        }

        Count count = getCount(context);

        StringBuilder sb = new StringBuilder();

        if (count.isDistinct()) {
            sb.append(DISTINCT_QUALIFIER).append(' ');
        }

        List<String> expressions = count.getExpressions();

        sb.append('(').append(expressions.get(0));
        for (int i = 1; i < expressions.size(); i++){
            sb.append(", ").append(expressions.get(i));
        }
        sb.append(')');

        renderer.start(context).addParameter(sb.toString()).build();
    }

    protected Count getCount(FunctionRenderContext context) {
        boolean distinct = false;
        int startIndex = 0;
        int argsSize = context.getArgumentsSize();
        String maybeDistinct = context.getArgument(0);

        if (("'" + DISTINCT_QUALIFIER + "'").equalsIgnoreCase(maybeDistinct)) {
            distinct = true;
            startIndex++;
        }

        if (startIndex >= argsSize) {
            throw new RuntimeException("The " + CountFunction.FUNCTION_NAME + " function needs at least one expression to count! args=" + context);
        }

        List<String> expressions = new ArrayList<String>();
        for (int i = startIndex; i < argsSize; i++) {
            expressions.add(context.getArgument(i));
        }

        return new Count(distinct, expressions);
    }

    protected static final class Count {

        private final boolean distinct;
        private final List<String> expressions;

        public Count(boolean distinct, List<String> expressions) {
            this.distinct = distinct;
            this.expressions = expressions;
        }

        public boolean isDistinct() {
            return distinct;
        }

        public List<String> getExpressions() {
            return expressions;
        }
    }
}
