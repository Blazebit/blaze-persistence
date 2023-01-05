/*
 * Copyright 2014 - 2023 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.criteria;

import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import java.util.List;

/**
 * An {@link javax.persistence.criteria.Expression} for an ordered set-aggregate function.
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
