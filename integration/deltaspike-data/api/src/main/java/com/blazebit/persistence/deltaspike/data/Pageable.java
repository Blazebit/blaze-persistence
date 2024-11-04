/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data;

/**
 * Interface containing pagination information, heavily inspired by Spring Data's <code>Pageable</code>.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface Pageable {

    /**
     * Returns the page to be returned. Zero based.
     *
     * @return The page to be returned.
     */
    public int getPageNumber();

    /**
     * Returns the number of elements per page to be returned.
     *
     * @return The number of elements per page
     */
    public int getPageSize();

    /**
     * Returns the offset represented by the page number multiplied with the page size.
     *
     * @return The offset
     */
    public int getOffset();

    /**
     * Returns the sorting that should be used for pagination.
     *
     * @return The sorting
     */
    public Sort getSort();

    /**
     * Returns the {@linkplain Pageable} that can be used to request the next {@link Page}.
     *
     * @return The {@linkplain Pageable} for the next {@linkplain Page}
     */
    public Pageable next();

    /**
     * Returns the {@linkplain Pageable} that can be used to request the previous {@link Page} or null if the current one already is the first one.
     *
     * @return The {@linkplain Pageable} for the previous {@linkplain Page}
     */
    public Pageable previous();

    /**
     * Returns the {@linkplain Pageable} that can be used to request the previous {@link Page} or the first if the current one already is the first one.
     *
     * @return The {@linkplain Pageable} for the previous or first {@linkplain Page}
     */
    public Pageable previousOrFirst();

    /**
     * Returns the {@linkplain Pageable} that can be used to request the first {@link Page}.
     *
     * @return The {@linkplain Pageable} for the first {@linkplain Page}
     */
    public Pageable first();

    /**
     * Returns whether there is a previous {@linkplain Pageable} i.e. if the page number is greater than 0.
     *
     * @return Whether there is a previous page
     */
    public boolean hasPrevious();
}