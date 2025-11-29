/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;

import jakarta.persistence.criteria.Expression;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class UnaryMinusExpression<T> extends AbstractExpression<T> {

    private static final long serialVersionUID = 1L;

    private final Expression<T> operand;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public UnaryMinusExpression(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<T> operand) {
        super(criteriaBuilder, (Class) operand.getJavaType());
        this.operand = operand;
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.visit(operand);
    }

    @Override
    public void render(RenderContext context) {
        context.getBuffer().append('-');
        context.apply(operand);
    }

}
