/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
