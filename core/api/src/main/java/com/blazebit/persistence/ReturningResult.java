/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

import java.util.List;

/**
 * A builder for modification queries.
 *
 * @param <T> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface ReturningResult<T> {
    
    /**
     * Returns the last element of the returning clause result. 
     * 
     * @return the last element of the returning clause result
     */
    // TODO: is this really necessary?
    public T getLastResult();

    /**
     * Returns the result of the returning clause.
     * 
     * Note that returning all elements might not be supported by all databases.
     *
     * @return The result of the returning clause
     */
    public List<T> getResultList();
    
    /**
     * Execute this modification statement and return the number of affected entities.
     * 
     * @return The number of affected entities
     */
    public int getUpdateCount();
    
}
