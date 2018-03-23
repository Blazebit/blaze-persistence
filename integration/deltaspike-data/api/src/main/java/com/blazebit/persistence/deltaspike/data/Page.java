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

package com.blazebit.persistence.deltaspike.data;

/**
 * A sublist of a list of objects, heavily inspired by Spring Data's <code>Page</code>.
 *
 * @param <T> The element type
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface Page<T> extends Slice<T> {

    /**
     * Returns the number of total pages.
     *
     * @return The number of total pages
     */
    public int getTotalPages();

    /**
     * Returns the total amount of elements.
     *
     * @return The total amount of elements
     */
    public long getTotalElements();
}
