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
