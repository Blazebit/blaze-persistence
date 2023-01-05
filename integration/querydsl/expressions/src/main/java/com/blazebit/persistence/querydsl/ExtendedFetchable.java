/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.querydsl;

import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.Queryable;
import com.querydsl.core.Fetchable;

/**
 * Extension for {@code Fetchable}
 *
 * @param <T> query return type.
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
public interface ExtendedFetchable<T> extends Fetchable<T>  {

    /**
     * Execute the query and return the result as a type PagedList.
     *
     * @param firstResult The position of the first result to retrieve, numbered from 0
     * @param maxResults The maximum number of results to retrieve
     * @return The paged list of the results
     * @see com.blazebit.persistence.FullQueryBuilder#page(int, int)
     */
    PagedList<T> fetchPage(int firstResult, int maxResults);

    /**
     * Execute the query and return the result as a type PagedList.
     *
     * @param keysetPage The key set from a previous result, may be null
     * @param firstResult The position of the first result to retrieve, numbered from 0
     * @param maxResults The maximum number of results to retrieve
     * @return The paged list of the results
     * @see com.blazebit.persistence.FullQueryBuilder#page(KeysetPage, int, int)
     */
    PagedList<T> fetchPage(KeysetPage keysetPage, int firstResult, int maxResults);

    /**
     * Get the query string.
     *
     * @return the query string
     * @see Queryable#getQueryString()
     */
    String getQueryString();

}
