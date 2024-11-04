/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.expression.function;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.AbstractExpression;
import com.blazebit.persistence.criteria.impl.path.AbstractPath;
import com.blazebit.persistence.criteria.impl.path.TreatedPath;

import java.io.Serializable;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class IndexFunction extends AbstractExpression<Integer> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final AbstractPath<?> origin;

    public IndexFunction(BlazeCriteriaBuilderImpl criteriaBuilder, AbstractPath<?> origin) {
        super(criteriaBuilder, Integer.class);
        if (origin instanceof TreatedPath<?>) {
            this.origin = ((TreatedPath<?>) origin).getTreatedPath();
        } else {
            this.origin = origin;
        }
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        // no-op
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        buffer.append("INDEX(");
        origin.renderPathExpression(context);
        buffer.append(')');
    }
}
