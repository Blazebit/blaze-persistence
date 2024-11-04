/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class BooleanLiteralPredicate extends AbstractSimplePredicate {

    private static final long serialVersionUID = 1L;

    private final Boolean value;

    public BooleanLiteralPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, Boolean value) {
        super(criteriaBuilder, false);
        this.value = value;
    }

    @Override
    public AbstractPredicate copyNegated() {
        return new BooleanLiteralPredicate(criteriaBuilder, !value);
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        // no-op
    }

    @Override
    public void render(RenderContext context) {
        if (value ^ isNegated()) {
            context.getBuffer().append("1=1");
        } else {
            context.getBuffer().append("1=0");
        }
    }

}
