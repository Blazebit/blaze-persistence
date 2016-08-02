package com.blazebit.persistence.criteria.impl.expression.function;

import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.LiteralExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AggregationFunction<T> extends FunctionExpressionImpl<T> {

    private static final long serialVersionUID = 1L;

    public AggregationFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> returnType, String functionName, Expression<?> argument) {
        super(criteriaBuilder, returnType, functionName, argument);
    }

    public static class COUNT extends AggregationFunction<Long> {

        private static final long serialVersionUID = 1L;
        private static final String NAME = "COUNT";

        private final boolean distinct;

        public COUNT(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<?> expression, boolean distinct) {
            super(criteriaBuilder, Long.class, NAME, expression);
            this.distinct = distinct;
        }

        @Override
        public void render(RenderContext context) {
            final StringBuilder buffer = context.getBuffer();
            if (isDistinct()) {
                List<Expression<?>> args = getArgumentExpressions();
                buffer.append(getFunctionName()).append('(');
                for (int i = 0; i < args.size(); i++) {
                    if (i != 0) {
                        buffer.append(',');
                    } else {
                        buffer.append("DISTINCT ");
                    }

                    context.apply(args.get(i));
                }
                buffer.append(')');
            } else {
                super.render(context);
            }
        }

        public boolean isDistinct() {
            return distinct;
        }

    }

    public static class AVG extends AggregationFunction<Double> {

        private static final long serialVersionUID = 1L;
        private static final String NAME = "AVG";

        public AVG(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<? extends Number> expression) {
            super(criteriaBuilder, Double.class, NAME, expression);
        }
    }

    public static class SUM<N extends Number> extends AggregationFunction<N> {

        private static final long serialVersionUID = 1L;
        private static final String NAME = "SUM";

        @SuppressWarnings({ "unchecked" })
        public SUM(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<N> expression) {
            super(criteriaBuilder, (Class<N>) expression.getJavaType(), NAME, expression);
            setJavaType(expression.getJavaType());
        }

        public SUM(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<? extends Number> expression, Class<N> returnType) {
            super(criteriaBuilder, returnType, NAME, expression);
            setJavaType(returnType);
        }
    }

    public static class MIN<N extends Number> extends AggregationFunction<N> {

        private static final long serialVersionUID = 1L;
        private static final String NAME = "MIN";

        @SuppressWarnings({ "unchecked" })
        public MIN(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<N> expression) {
            super(criteriaBuilder, (Class<N>) expression.getJavaType(), NAME, expression);
        }
    }

    public static class MAX<N extends Number> extends AggregationFunction<N> {

        private static final long serialVersionUID = 1L;
        private static final String NAME = "MAX";

        @SuppressWarnings({ "unchecked" })
        public MAX(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<N> expression) {
            super(criteriaBuilder, (Class<N>) expression.getJavaType(), NAME, expression);
        }
    }

    public static class LEAST<X extends Comparable<X>> extends AggregationFunction<X> {

        private static final long serialVersionUID = 1L;
        private static final String NAME = "MIN";

        @SuppressWarnings({ "unchecked" })
        public LEAST(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<X> expression) {
            super(criteriaBuilder, (Class<X>) expression.getJavaType(), NAME, expression);
        }
    }

    public static class GREATEST<X extends Comparable<X>> extends AggregationFunction<X> {

        private static final long serialVersionUID = 1L;
        private static final String NAME = "MAX";

        @SuppressWarnings({ "unchecked" })
        public GREATEST(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<X> expression) {
            super(criteriaBuilder, (Class<X>) expression.getJavaType(), NAME, expression);
        }
    }
}
