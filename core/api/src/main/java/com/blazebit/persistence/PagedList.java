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
 * An extended version of a {@linkplain List} which also provides access to the total size of the list.
 *
 * @param <T> the type of elements in this list
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface PagedList<T> extends List<T> {

    /**
     * An empty paged list.
     *
     * @since 1.2.0
     */
    @SuppressWarnings("rawtypes")
    public static final PagedList EMPTY = new EmptyPagedList<>();

    /**
     * Returns the actual size of this page.
     *
     * @return The actual size
     * @see List#size()
     */
    public int getSize();

    /**
     * Returns the total size of the list or <code>-1</code> if the count query was disabled via {@link PaginatedCriteriaBuilder#withCountQuery(boolean)}.
     *
     * @return The total size or <code>-1</code> if the count query was disabled
     */
    public long getTotalSize();

    /**
     * Returns the number of this page, numbered from 1.
     * 
     * @return The number of this page
     */
    public int getPage();

    /**
     * Returns the number of total pages.
     * 
     * @return The number of total pages
     */
    public int getTotalPages();

    /**
     * Returns the position of the first result, numbered from 0.
     * This is the position which was actually queried. This value might be different from {@linkplain KeysetPage#getFirstResult()}.
     * 
     * If this list was queried with an entity id which does not exist, this will return <code>-1</code>;
     *
     * @return The position of the first result or <code>-1</code> if the queried entity id does not exist
     */
    public int getFirstResult();

    /**
     * Returns the maximum number of results.
     * This is the maximum number which was actually queried. This value might be different from
     * {@linkplain KeysetPage#getFirstResult()}.
     *
     * @return The maximum number of results
     */
    public int getMaxResults();

    /**
     * Returns the key set page for this paged list which can be used for key set pagination.
     * The key set page may be null if key set pagination wasn't used.
     *
     * @return The key set
     * @see FullQueryBuilder#page(com.blazebit.persistence.KeysetPage, int, int)
     */
    public KeysetPage getKeysetPage();

}
