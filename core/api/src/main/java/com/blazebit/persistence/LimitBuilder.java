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

package com.blazebit.persistence;

/**
 * An interface for builders that support limit and offset.
 * This is related to the fact, that a query builder supports the limit and offset clause.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface LimitBuilder<X extends LimitBuilder<X>> {

    /**
     * Set the position of the first result to retrieve.
     *
     * @param firstResult The position of the first result, numbered from 0
     * @return This builder for chaining
     */
    public X setFirstResult(int firstResult);

    /**
     * Set the maximum number of results to retrieve.
     *
     * @param maxResults The maximum number of results to retrieve
     * @return This builder for chaining
     */
    public X setMaxResults(int maxResults);

    /**
     * The position of the first result.
     * Returns 0 if <code>setFirstResult</code> was not used.
     *
     * @return The position of the first result
     */
    public int getFirstResult();

    /**
     * The maximum number of results to retrieve.
     * Returns <code>Integer.MAX_VALUE</code> if <code>setMaxResults</code> was not used.
     *
     * @return The maximum number of results
     */
    public int getMaxResults();
    
}
