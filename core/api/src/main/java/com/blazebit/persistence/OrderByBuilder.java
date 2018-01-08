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
 * An interface for builders that support sorting.
 * This is related to the fact, that a query builder supports order by clauses.
 *
 * @param <T> The concrete builder type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface OrderByBuilder<T extends OrderByBuilder<T>> {

    /**
     * Adds an order by clause with the given expression to the query.
     *
     * @param expression The expression for the order by clause
     * @param ascending Wether the order should be ascending or descending.
     * @param nullFirst Wether null elements should be ordered first or not.
     * @return The query builder for chaining calls
     */
    public T orderBy(String expression, boolean ascending, boolean nullFirst);

    /**
     * Like {@link BaseQueryBuilder#orderByAsc(java.lang.String, boolean) } but with <code>nullFirst</code> set to false.
     *
     * @param expression The expression for the order by clause
     * @return The query builder for chaining calls
     */
    public T orderByAsc(String expression);

    /**
     * Like {@link BaseQueryBuilder#orderBy(java.lang.String, boolean, boolean) } but with <code>ascending</code> set to true.
     *
     * @param expression The expression for the order by clause
     * @param nullFirst Wether null elements should be ordered first or not.
     * @return The query builder for chaining calls
     */
    public T orderByAsc(String expression, boolean nullFirst);

    /**
     * Like {@link BaseQueryBuilder#orderByDesc(java.lang.String, boolean) } but with <code>nullFirst</code> set to false.
     *
     * @param expression The expression for the order by clause
     * @return The query builder for chaining calls
     */
    public T orderByDesc(String expression);

    /**
     * Like {@link BaseQueryBuilder#orderBy(java.lang.String, boolean, boolean) } but with <code>ascending</code> set to false.
     *
     * @param expression The expression for the order by clause
     * @param nullFirst Wether null elements should be ordered first or not.
     * @return The query builder for chaining calls
     */
    public T orderByDesc(String expression, boolean nullFirst);

    // NOTE: JPA 4.6.16 says that subqueries are only allowed in WHERE and HAVING
    // TODO: order by subqueries? 
}
