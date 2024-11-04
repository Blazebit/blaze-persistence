/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.expression.function;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.AbstractExpression;

import javax.persistence.criteria.Expression;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class NullifFunction<T> extends AbstractExpression<T> {

    private static final long serialVersionUID = 1L;

    private final Expression<? extends T> expression1;
    private final Expression<?> expression2;

    public NullifFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> javaType, Expression<? extends T> expression1, Expression<?> expression2) {
        super(criteriaBuilder, determineType(javaType, expression1));
        this.expression1 = expression1;
        this.expression2 = expression2;
    }

    @SuppressWarnings({"unchecked"})
    private static <T> Class<T> determineType(Class<T> javaType, Expression expression) {
        return javaType != null ? javaType : (Class<T>) expression.getJavaType();
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.visit(expression1);
        visitor.visit(expression2);
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        buffer.append("NULLIF(");
        context.apply(expression1);
        buffer.append(',');
        context.apply(expression2);
        buffer.append(')');
    }

}
