/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractPredicate extends AbstractExpression<Boolean> implements Predicate {

    private static final long serialVersionUID = 1L;

    private final boolean negated;

    protected AbstractPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated) {
        super(criteriaBuilder, Boolean.class);
        this.negated = negated;
    }

    public boolean isNegated() {
        return negated;
    }

    @Override
    public Predicate not() {
        return criteriaBuilder.negate(this);
    }

    public abstract AbstractPredicate copyNegated();

    @Override
    public final boolean isCompoundSelection() {
        return false;
    }

    @Override
    public final List<Selection<?>> getCompoundSelectionItems() {
        throw new IllegalStateException("Not a compound selection");
    }
}
