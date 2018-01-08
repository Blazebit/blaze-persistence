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
 * An interface for builders that support set operators.
 *
 * @param <X> The concrete builder type
 * @param <Y> The builder type for connecting a nested query
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface SetOperationBuilder<X, Y extends StartOngoingSetOperationBuilder<?, ?, ?>> {

    /**
     * Connects this query with the union operator with query following after this call. 
     *
     * @return The query builder that should be connected via union
     */
    public X union();

    /**
     * Connects this query with the union all operator with query following after this call. 
     *
     * @return The query builder that should be connected via union
     */
    public X unionAll();

    /**
     * Connects this query with the intersect operator with query following after this call. 
     *
     * @return The query builder that should be connected via union
     */
    public X intersect();

    /**
     * Connects this query with the intersect all operator with query following after this call. 
     *
     * @return The query builder that should be connected via union
     */
    public X intersectAll();

    /**
     * Connects this query with the except operator with query following after this call. 
     *
     * @return The query builder that should be connected via union
     */
    public X except();

    /**
     * Connects this query with the except all operator with query following after this call. 
     *
     * @return The query builder that should be connected via union
     */
    public X exceptAll();
    
    /* Subquery variants */

    /**
     * Connects this query with the union operator with subquery following after this call. 
     *
     * @return The query builder that should be connected via union
     */
    public Y startUnion();

    /**
     * Connects this query with the union all operator with subquery following after this call. 
     *
     * @return The query builder that should be connected via union
     */
    public Y startUnionAll();

    /**
     * Connects this query with the intersect operator with subquery following after this call. 
     *
     * @return The query builder that should be connected via union
     */
    public Y startIntersect();

    /**
     * Connects this query with the intersect all operator with subquery following after this call. 
     *
     * @return The query builder that should be connected via union
     */
    public Y startIntersectAll();

    /**
     * Connects this query with the except operator with subquery following after this call. 
     *
     * @return The query builder that should be connected via union
     */
    public Y startExcept();

    /**
     * Connects this query with the except all operator with subquery following after this call. 
     *
     * @return The query builder that should be connected via union
     */
    public Y startExceptAll();
}
