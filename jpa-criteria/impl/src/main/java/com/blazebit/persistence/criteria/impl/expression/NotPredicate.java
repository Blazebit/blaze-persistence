/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class NotPredicate extends AbstractSimplePredicate {

    private final AbstractPredicate predicate;

    public NotPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, AbstractPredicate predicate) {
        super(criteriaBuilder, false);
        this.predicate = predicate;
    }

    @Override
    public AbstractPredicate copyNegated() {
        return new NotPredicate(criteriaBuilder, this);
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        boolean requiresParenthesis = predicate instanceof CompoundPredicate;
        buffer.append("NOT ");
        if (requiresParenthesis) {
            buffer.append("(");
            context.apply(predicate);
            buffer.append(")");
        } else {
            context.apply(predicate);
        }
    }
}
