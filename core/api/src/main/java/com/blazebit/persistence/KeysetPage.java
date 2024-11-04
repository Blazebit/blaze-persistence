/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

import java.io.Serializable;
import java.util.List;

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

    /**
     * Returns the key set list of the corresponding {@link PagedList}.
     *
     * @return The key set list
     * @since 1.4.0
     */
    public List<Keyset> getKeysets();
}
