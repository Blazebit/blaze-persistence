/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import java.util.List;

/**
 * An {@link jakarta.persistence.criteria.Expression} for an ordered set-aggregate function.
 *
 * @param <X> The target type
 * @author Christian Beikov
 * @since 1.6.4
 */
public interface BlazeOrderedSetAggregateFunctionExpression<X> extends BlazeAggregateFunctionExpression<X> {

    /**
     * Returns the within group order for this ordered set-aggregate function.
     *
     * @return the within group order
     */
    public List<BlazeOrder> getWithinGroup();

    /**
     * Sets the within group order for this ordered set-aggregate function.
     *
     * @param orders The order within which the aggregate should work
     * @return <code>this</code> for method chaining
     */
    public BlazeOrderedSetAggregateFunctionExpression<X> withinGroup(Order... orders);

    /**
     * Sets the within group order for this ordered set-aggregate function.
     *
     * @param orders The order within which the aggregate should work
     * @return <code>this</code> for method chaining
     */
    public BlazeOrderedSetAggregateFunctionExpression<X> withinGroup(List<? extends Order> orders);

    @Override
    public BlazeOrderedSetAggregateFunctionExpression<X> filter(Predicate filter);

    @Override
    public BlazeOrderedSetAggregateFunctionExpression<X> window(BlazeWindow window);
}
