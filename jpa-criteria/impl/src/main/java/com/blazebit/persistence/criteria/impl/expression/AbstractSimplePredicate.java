/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;

import javax.persistence.criteria.Expression;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractSimplePredicate extends AbstractPredicate {

    private static final long serialVersionUID = 1L;
    private static final List<Expression<Boolean>> NO_EXPRESSIONS = Collections.emptyList();

    public AbstractSimplePredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated) {
        super(criteriaBuilder, negated);
    }

    public BooleanOperator getOperator() {
        return BooleanOperator.AND;
    }

    public final List<Expression<Boolean>> getExpressions() {
        return NO_EXPRESSIONS;
    }
}
