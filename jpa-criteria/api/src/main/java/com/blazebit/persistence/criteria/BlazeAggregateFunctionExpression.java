/*
 * Copyright 2014 - 2021 Blazebit.
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

import javax.persistence.criteria.Predicate;

/**
 * An {@link javax.persistence.criteria.Expression} for an aggregate function.
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
