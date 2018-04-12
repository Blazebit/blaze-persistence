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
 * A sublist of a list of objects without knowledge of the size of the overall list, heavily inspired by Spring Data's <code>Slice</code>.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface Slice<T> extends Iterable<T> {

    /**
     * Returns the number of the current {@linkplain Slice}. Zero based.
     *
     * @return The number of the current {@linkplain Slice}.
     */
    public int getNumber();

    /**
     * Returns the size of the {@linkplain Slice} i.e. what was request via {@link Pageable#getPageSize()}.
     *
     * @return The size of the {@linkplain Slice}.
     */
    public int getSize();

    /**
     * Returns the actual number of elements currently on this {@linkplain Slice}.
     *
     * @return The actual number of elements currently on this {@linkplain Slice}.
     */
    public int getNumberOfElements();

    /**
     * Returns the slice content as {@linkplain Slice}.
     *
     * @return The content of the {@linkplain Slice}
     */
    public List<T> getContent();

    /**
     * Returns whether the {@linkplain Slice} has content at all.
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
     * Returns whether the current {@linkplain Slice} is the first one.
     *
     * @return <code>true</code> if the {@link #getNumber()} is <code>0</code>, <code>false</code> otherwise
     */
    public boolean isFirst();

    /**
     * Returns whether the current {@linkplain Slice} is the last one.
     *
     * @return <code>true</code> if this is the last slice, <code>false</code> otherwise
     */
    public boolean isLast();

    /**
     * Returns if there is a next {@linkplain Slice}.
     *
     * @return <code>true</code> if {@link #nextPageable()} will return results, <code>false</code> otherwise
     */
    public boolean hasNext();

    /**
     * Returns if there is a previous {@linkplain Slice}.
     *
     * @return <code>true</code> if the {@link #getNumber()} is greater than <code>0</code>, <code>false</code> otherwise
     */
    public boolean hasPrevious();

    /**
     * Returns the {@link Pageable} that can be used to request the current {@linkplain Slice}.
     *
     * @return The {@linkplain Pageable} for the current {@linkplain Slice}
     */
    public Pageable currentPageable();

    /**
     * Returns the {@link Pageable} that can be used to request the next {@linkplain Slice}.
     * Returns <code>null</code> if {@link #hasNext()} returns <code>false</code>.
     *
     * @return The {@linkplain Pageable} for the next {@linkplain Slice}, or <code>null</code> if this is the last Slice
     */
    public Pageable nextPageable();

    /**
     * Returns the {@link Pageable} that can be used to request the previous {@linkplain Slice}.
     * Returns <code>null</code> if {@link #hasPrevious()} returns <code>false</code>.
     *
     * @return The {@linkplain Pageable} for the previous {@linkplain Slice}, or <code>null</code> if this is the first Slice
     */
    public Pageable previousPageable();

}
