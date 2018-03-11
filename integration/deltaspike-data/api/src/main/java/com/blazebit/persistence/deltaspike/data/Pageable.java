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