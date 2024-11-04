/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
