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

/**
 * A builder for paginated criteria queries.
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface PaginatedCriteriaBuilder<T> extends FullQueryBuilder<T, PaginatedCriteriaBuilder<T>> {

    /**
     * Returns the paginated typed query for the built query.
     * The returned query is already parameterized with all known parameters.
     *
     * @return The paginated typed query for the built query
     */
    public PaginatedTypedQuery<T> getQuery();

    /**
     * Returns the query string that selects the count of elements.
     *
     * @return The query string
     */
    public String getPageCountQueryString();

    /**
     * Returns the query string that selects the id of the elements.
     *
     * @return The query string
     */
    public String getPageIdQueryString();

    /**
     * Enable or disables keyset extraction which influences whether {@link PagedList#getKeysetPage()} is available.
     * 
     * @param keysetExtraction true to enable, false to disable keyset extraction
     * @return The query builder for chaining calls
     */
    public PaginatedCriteriaBuilder<T> withKeysetExtraction(boolean keysetExtraction);

    /**
     * Returns whether keyset extraction is enabled or not.
     * 
     * @return true when enabled, false otherwise
     */
    public boolean isKeysetExtraction();

    /**
     * Enables or disables execution of the count query which determines whether {@link PagedList#getTotalSize()} is available.
     *
     * @param withCountQuery true to enable, false to disable the count query execution
     * @return The query builder for chaining calls
     * @since 1.2.0
     */
    public PaginatedCriteriaBuilder<T> withCountQuery(boolean withCountQuery);

    /**
     * Returns whether count query execution is enabled or not.
     *
     * @return true when enabled, false otherwise
     * @since 1.2.0
     */
    public boolean isWithCountQuery();

    /**
     * Sets the offset for the highest keyset which influences which element of a page is returned by {@link KeysetPage#getHighest()}.
     * This is usually used when loading N + 1 rows to know there are further rows but only needing N rows.
     *
     * @param offset the offset for the highest keyset relative to the page size
     * @return The query builder for chaining calls
     * @since 1.2.0
     */
    public PaginatedCriteriaBuilder<T> withHighestKeysetOffset(int offset);

    /**
     * Returns the offset of the highest keyset relative to the page size.
     *
     * @return the offset for the highest keyset relative to the page size
     * @since 1.2.0
     */
    public int getHighestKeysetOffset();

    /**
     * Execute the query and return the result as a type PagedList.
     *
     * @return The paged list of the results
     */
    @Override
    public PagedList<T> getResultList();

    @Override
    public <Y> PaginatedCriteriaBuilder<Y> copy(Class<Y> resultClass);

    @Override
    public <Y> SelectObjectBuilder<PaginatedCriteriaBuilder<Y>> selectNew(Class<Y> clazz);

    @Override
    public <Y> PaginatedCriteriaBuilder<Y> selectNew(ObjectBuilder<Y> builder);

}
