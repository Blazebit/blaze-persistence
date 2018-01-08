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
