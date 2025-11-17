/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

import jakarta.persistence.criteria.Predicate;

/**
 * An {@link jakarta.persistence.criteria.Expression} for an aggregate function.
 *
 * @param <X> The target type
 * @author Christian Beikov
 * @since 1.6.4
 */
public interface BlazeAggregateFunctionExpression<X> extends BlazeWindowFunctionExpression<X> {

    /**
     * Returns the filter for this aggregate function.
     *
     * @return the filter
     */
    public Predicate getFilter();

    /**
     * Sets the filter for this aggregate function.
     *
     * @param filter The filter to set
     * @return <code>this</code> for method chaining
     */
    public BlazeAggregateFunctionExpression<X> filter(Predicate filter);

    @Override
    public BlazeAggregateFunctionExpression<X> window(BlazeWindow window);
}
