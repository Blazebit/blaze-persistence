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

package com.blazebit.persistence.criteria.impl.expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;

import javax.persistence.criteria.Expression;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class BinaryArithmeticExpression<N extends Number> extends AbstractExpression<N> {

    private static final long serialVersionUID = 1L;

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static enum Operation {
        ADD {
            public void render(RenderContext context, Expression<?> leftHandSide, Expression<?> rightHandSide) {
                simple(context, leftHandSide, '+', rightHandSide);
            }
        },
        SUBTRACT {
            public void render(RenderContext context, Expression<?> leftHandSide, Expression<?> rightHandSide) {
                simple(context, leftHandSide, '-', rightHandSide);
            }
        },
        MULTIPLY {
            public void render(RenderContext context, Expression<?> leftHandSide, Expression<?> rightHandSide) {
                simple(context, leftHandSide, '*', rightHandSide);
            }
        },
        DIVIDE {
            public void render(RenderContext context, Expression<?> leftHandSide, Expression<?> rightHandSide) {
                simple(context, leftHandSide, '/', rightHandSide);
            }
        },
        MOD {
            public void render(RenderContext context, Expression<?> leftHandSide, Expression<?> rightHandSide) {
                context.getBuffer().append("MOD(");
                context.apply(leftHandSide);
                context.getBuffer().append(',');
                context.apply(rightHandSide);
                context.getBuffer().append(')');
            }
        };

        public abstract void render(RenderContext context, Expression<?> leftHandSide, Expression<?> rightHandSide);

        private static void simple(RenderContext context, Expression<?> leftHandSide, char operator, Expression<?> rightHandSide) {
            context.getBuffer().append('(');
            context.apply(leftHandSide);
            context.getBuffer().append(operator);
            context.apply(rightHandSide);
            context.getBuffer().append(')');
        }
    }

    private final Operation operator;
    private final Expression<? extends N> rightHandSide;
    private final Expression<? extends N> leftHandSide;

    public BinaryArithmeticExpression(BlazeCriteriaBuilderImpl criteriaBuilder, Class<N> resultType, Operation operator, Expression<? extends N> leftHandSide, Expression<? extends N> rightHandSide) {
        super(criteriaBuilder, resultType);
        this.operator = operator;
        this.leftHandSide = leftHandSide;
        this.rightHandSide = rightHandSide;
    }

    public BinaryArithmeticExpression(BlazeCriteriaBuilderImpl criteriaBuilder, Class<N> javaType, Operation operator, Expression<? extends N> leftHandSide, N rightHandSide) {
        super(criteriaBuilder, javaType);
        this.operator = operator;
        this.leftHandSide = leftHandSide;
        this.rightHandSide = new LiteralExpression<N>(criteriaBuilder, rightHandSide);
    }

    public BinaryArithmeticExpression(BlazeCriteriaBuilderImpl criteriaBuilder, Class<N> javaType, Operation operator, N leftHandSide, Expression<? extends N> rightHandSide) {
        super(criteriaBuilder, javaType);
        this.operator = operator;
        this.leftHandSide = new LiteralExpression<N>(criteriaBuilder, leftHandSide);
        this.rightHandSide = rightHandSide;
    }

    public static Class<? extends Number> determineResultType(Class<? extends Number> argument1Type, Class<? extends Number> argument2Type) {
        return determineResultType(argument1Type, argument2Type, false);
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Number> determineResultType(Class<? extends Number> argument1Type, Class<? extends Number> argument2Type, boolean isQuotientOperation) {
        if (isQuotientOperation) {
            return Number.class;
        }
        return determineResultType(new Class[]{argument1Type, argument2Type});
    }

    /**
     * Apply rules from "6.5.7.1 Result Types of Expressions" of the JPA spec.
     */
    public static Class<? extends Number> determineResultType(Class<? extends Number>... types) {
        Class<? extends Number> result = Number.class;

        for (Class<? extends Number> type : types) {
            if (Double.class.equals(type)) {
                result = Double.class;
            } else if (Float.class.equals(type)) {
                result = Float.class;
            } else if (BigDecimal.class.equals(type)) {
                result = BigDecimal.class;
            } else if (BigInteger.class.equals(type)) {
                result = BigInteger.class;
            } else if (Long.class.equals(type)) {
                result = Long.class;
            } else if (isIntegralType(type)) {
                result = Integer.class;
            }
        }

        return result;
    }

    private static boolean isIntegralType(Class<? extends Number> type) {
        return Integer.class.equals(type) || Short.class.equals(type);
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.visit(leftHandSide);
        visitor.visit(rightHandSide);
    }

    @Override
    public void render(RenderContext context) {
        operator.render(context, leftHandSide, rightHandSide);
    }

}
