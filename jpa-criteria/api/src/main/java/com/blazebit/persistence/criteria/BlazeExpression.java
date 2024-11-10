/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

import jakarta.persistence.criteria.Expression;

/**
 * An extended version of {@link Expression}.
 *
 * @param <X> The target type
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeExpression<X> extends Expression<X> {

    /* Covariant overrides */

    /**
     * Like {@link Expression#as} but returns the subtype {@link BlazeExpression} instead.
     *
     * @param type intended type of the expression
     * @param <T>  The intended expression type
     * @return A new expression of the given type
     */
    <T> BlazeExpression<T> as(Class<T> type);
}
