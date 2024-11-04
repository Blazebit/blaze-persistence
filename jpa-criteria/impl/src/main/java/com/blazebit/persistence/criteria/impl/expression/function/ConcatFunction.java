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
public class ConcatFunction extends AbstractExpression<String> {

    private static final long serialVersionUID = 1L;

    private final Expression<String> string1;
    private final Expression<String> string2;

    public ConcatFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> expression1, Expression<String> expression2) {
        super(criteriaBuilder, String.class);
        this.string1 = expression1;
        this.string2 = expression2;
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.visit(string1);
        visitor.visit(string2);
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        buffer.append("CONCAT(");
        context.apply(string1);
        buffer.append(',');
        context.apply(string2);
        buffer.append(')');
    }

}
