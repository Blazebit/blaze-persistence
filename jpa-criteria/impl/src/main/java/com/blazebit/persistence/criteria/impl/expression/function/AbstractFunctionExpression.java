/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.expression.function;

import com.blazebit.persistence.criteria.BlazeFunctionExpression;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.AbstractExpression;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractFunctionExpression<X> extends AbstractExpression<X> implements BlazeFunctionExpression<X> {

    private static final long serialVersionUID = 1L;

    private final String functionName;

    public AbstractFunctionExpression(BlazeCriteriaBuilderImpl criteriaBuilder, Class<X> javaType, String functionName) {
        super(criteriaBuilder, javaType);
        this.functionName = functionName;
    }

    @Override
    public String getFunctionName() {
        return functionName;
    }

    @Override
    public void render(RenderContext context) {
        context.getBuffer().append(getFunctionName()).append("()");
    }

}
