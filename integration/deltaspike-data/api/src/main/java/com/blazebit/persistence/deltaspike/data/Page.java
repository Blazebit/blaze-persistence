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

import java.util.List;

/**
 * A sublist of a list of objects, heavily inspired by Spring Data's <code>Page</code>.
 *
 * @param <T> The element type
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface Page<T> extends Iterable<T> {

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

    /**
     * Returns the number of the current {@linkplain Page}. Zero based.
     *
     * @return The number of the current {@linkplain Page}.
     */
    public int getNumber();

    /**
     * Returns the size of the {@linkplain Page} i.e. what was request via {@link Pageable#getPageSize()}.
     *
     * @return The size of the {@linkplain Page}.
     */
    public int getSize();

    /**
     * Returns the actual number of elements currently on this {@linkplain Page}.
     *
     * @return The actual number of elements currently on this {@linkplain Page}.
     */
    public int getNumberOfElements();

    /**
     * Returns the page content as {@linkplain Page}.
     *
     * @return The content of the {@linkplain Page}
     */
    public List<T> getContent();

    /**
     * Returns whether the {@linkplain Page} has content at all.
     *
     * @return <code>true</code> if the content is non-empty, <code>false</code> otherwise
     */
    public boolean hasContent();

    /**
     * Returns the sorting that was used for pagination.
     *
     * @return The sorting
     */
    public Sort getSort();

    /**
     * Returns whether the current {@linkplain Page} is the first one.
     *
     * @return <code>true</code> if the {@link #getNumber()} is <code>0</code>, <code>false</code> otherwise
     */
    public boolean isFirst();

    /**
     * Returns whether the current {@linkplain Page} is the last one.
     *
     * @return <code>true</code> if the {@link #getNumber()} is {@link #getTotalPages()}<code> - 1</code>, <code>false</code> otherwise
     */
    public boolean isLast();

    /**
     * Returns if there is a next {@linkplain Page}.
     *
     * @return <code>true</code> if the {@link #getNumber()} is less than {@link #getTotalPages()}<code> - 1</code>, <code>false</code> otherwise
     */
    public boolean hasNext();

    /**
     * Returns if there is a previous {@linkplain Page}.
     *
     * @return <code>true</code> if the {@link #getNumber()} is greater than <code>0</code>, <code>false</code> otherwise
     */
    public boolean hasPrevious();

    /**
     * Returns the {@link Pageable} that can be used to request the current {@linkplain Page}.
     *
     * @return The {@linkplain Pageable} for the current {@linkplain Page}
     */
    public Pageable currentPageable();

    /**
     * Returns the {@link Pageable} that can be used to request the next {@linkplain Page}.
     * Returns <code>null</code> if {@link #hasNext()} returns <code>false</code>.
     *
     * @return The {@linkplain Pageable} for the next {@linkplain Page}, or <code>null</code> if this is the last page
     */
    public Pageable nextPageable();

    /**
     * Returns the {@link Pageable} that can be used to request the previous {@linkplain Page}.
     * Returns <code>null</code> if {@link #hasPrevious()} returns <code>false</code>.
     *
     * @return The {@linkplain Pageable} for the previous {@linkplain Page}, or <code>null</code> if this is the first page
     */
    public Pageable previousPageable();
}
