/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

import jakarta.persistence.Query;

/**
 * A base interface for executable query builders.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface Executable {

    /**
     * Returns the query string for the built query.
     *
     * @return The query string
     */
    public String getQueryString();

    /**
     * Returns the JPA query for the built query.
     * The returned query is already parameterized with all known parameters.
     *
     * @return The typed query for the built query
     */
    public Query getQuery();
    
    /**
     * Execute this modification statement and return the number of affected entities.
     * 
     * @return The number of affected entities
     */
    public int executeUpdate();
}
