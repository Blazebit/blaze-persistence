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

import java.io.Serializable;

/**
 * An interface that represents the key set of a {@link PagedList}.
 * Instances of this interface can be used for key set pagination.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface KeysetPage extends Serializable {

    /**
     * Returns the position of the first result, numbered from 0.
     * This is the position of the first element of this key set.
     *
     * @return The position of the first result
     */
    public int getFirstResult();

    /**
     * Returns the maximum number of results.
     * This is the maximum number of results of this key set.
     *
     * @return The maximum number of results
     */
    public int getMaxResults();

    /**
     * Returns the key set for the lowest entry of the corresponding {@link PagedList}.
     *
     * @return The key set for the lowest entry
     */
    public Keyset getLowest();

    /**
     * Returns the key set for the highest entry of the corresponding {@link PagedList}.
     *
     * @return The key set for the highest entry
     */
    public Keyset getHighest();
}
