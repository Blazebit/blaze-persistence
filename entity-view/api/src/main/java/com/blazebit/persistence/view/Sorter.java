/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.view;

import com.blazebit.persistence.OrderByBuilder;

/**
 * A sorter is an object that applies an order by on a {@link OrderByBuilder} for a specific expression.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface Sorter {

    /**
     * Applies an order by on the given sortable for the given expression.
     *
     * @param <T>        The actual type of the sortable
     * @param sortable   The sortable on which to apply the order by
     * @param expression The order by expression
     * @return The sortable
     */
    public <T extends OrderByBuilder<T>> T apply(T sortable, String expression);

}
