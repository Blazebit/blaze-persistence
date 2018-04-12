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

import javax.persistence.TypedQuery;
import java.util.List;

/**
 * An extended version of a {@linkplain TypedQuery} which also provides access to a count query.
 *
 * @param <T> the return type of elements
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface PaginatedTypedQuery<T> extends TypedQuery<T> {

    /**
     * Executes a query to calculate the total count of elements and returns that count.
     *
     * @return the total count of elements
     */
    public long getTotalCount();

    /**
     * Returns the result list of the page without executing a count query.
     *
     * @return The result list of the requested page
     */
    public List<T> getPageResultList();

    /**
     * Returns a {@link PagedList} containing the result list of the requested page and optionally the total count depending on {@link PaginatedCriteriaBuilder#withCountQuery(boolean)}.
     *
     * @return The result as paged list
     */
    @Override
    public PagedList<T> getResultList();

}
