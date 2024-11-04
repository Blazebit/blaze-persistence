/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
