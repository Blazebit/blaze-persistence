/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * An interface for builders that support distinct.
 * This is related to the fact, that a query builder supports distinct selects.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface DistinctBuilder<X extends DistinctBuilder<X>> {

    /**
     * Marks the query to do a distinct select.
     *
     * @return The query builder for chaining calls
     */
    public X distinct();
}
