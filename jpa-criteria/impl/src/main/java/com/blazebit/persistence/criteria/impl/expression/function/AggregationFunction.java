/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.criteria.impl.expression.function;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;

import javax.persistence.criteria.Expression;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AggregationFunction<T> extends FunctionExpressionImpl<T> {

    private static final long serialVersionUID = 1L;

    public AggregationFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> returnType, String functionName, Expression<?> argument) {
        super(criteriaBuilder, returnType, functionName, argument);
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
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

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static class AVG extends AggregationFunction<Double> {

        private static final long serialVersionUID = 1L;
        private static final String NAME = "AVG";

        public AVG(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<? extends Number> expression) {
            super(criteriaBuilder, Double.class, NAME, expression);
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static class SUM<N extends Number> extends AggregationFunction<N> {

        private static final long serialVersionUID = 1L;
        private static final String NAME = "SUM";

        @SuppressWarnings({"unchecked"})
        public SUM(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<N> expression) {
            super(criteriaBuilder, (Class<N>) expression.getJavaType(), NAME, expression);
            setJavaType(expression.getJavaType());
        }

        public SUM(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<? extends Number> expression, Class<N> returnType) {
            super(criteriaBuilder, returnType, NAME, expression);
            setJavaType(returnType);
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static class MIN<N extends Number> extends AggregationFunction<N> {

        private static final long serialVersionUID = 1L;
        private static final String NAME = "MIN";

        @SuppressWarnings({"unchecked"})
        public MIN(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<N> expression) {
            super(criteriaBuilder, (Class<N>) expression.getJavaType(), NAME, expression);
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static class MAX<N extends Number> extends AggregationFunction<N> {

        private static final long serialVersionUID = 1L;
        private static final String NAME = "MAX";

        @SuppressWarnings({"unchecked"})
        public MAX(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<N> expression) {
            super(criteriaBuilder, (Class<N>) expression.getJavaType(), NAME, expression);
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static class LEAST<X extends Comparable<? super X>> extends AggregationFunction<X> {

        private static final long serialVersionUID = 1L;
        private static final String NAME = "MIN";

        @SuppressWarnings({"unchecked"})
        public LEAST(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<X> expression) {
            super(criteriaBuilder, (Class<X>) expression.getJavaType(), NAME, expression);
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static class GREATEST<X extends Comparable<? super X>> extends AggregationFunction<X> {

        private static final long serialVersionUID = 1L;
        private static final String NAME = "MAX";

        @SuppressWarnings({"unchecked"})
        public GREATEST(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<X> expression) {
            super(criteriaBuilder, (Class<X>) expression.getJavaType(), NAME, expression);
        }
    }
}
