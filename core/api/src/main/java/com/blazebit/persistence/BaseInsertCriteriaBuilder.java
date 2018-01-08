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
 * A base builder for insert queries.
 *
 * @param <T> The entity type for which this update query is
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface BaseInsertCriteriaBuilder<T, X extends BaseInsertCriteriaBuilder<T, X>> extends BaseModificationCriteriaBuilder<X>, FromBuilder<X>, KeysetQueryBuilder<X>, OrderByBuilder<X>, GroupByBuilder<X>, DistinctBuilder<X>, LimitBuilder<X> {

    /**
     * Binds the given value as parameter to the attribute.
     *
     * @param attribute The attribute for which the value should be bound
     * @param value The value that should be bound
     * @return The query builder for chaining calls
     */
    public X bind(String attribute, Object value);

    /**
     * Starts a select builder for creating an expression that should be bound to the attribute.
     *
     * @param attribute The attribute for which the select expression should be bound
     * @return The query builder for chaining calls
     */
    public SelectBuilder<X> bind(String attribute);
    
}
