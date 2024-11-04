/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * An interface for builders that support fetching.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface FetchBuilder<X extends FetchBuilder<X>> {

    /**
     * Adds an implicit join fetch to the query.
     *
     * @param path The path to join fetch
     * @return The query builder for chaining calls
     */
    public X fetch(String path);

    /**
     * Adds an implicit join fetch for every given path to the query.
     *
     * @param paths The paths to join fetch
     * @return The query builder for chaining calls
     */
    public X fetch(String... paths);
}
