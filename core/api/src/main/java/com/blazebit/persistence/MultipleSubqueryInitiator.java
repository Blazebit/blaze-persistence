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
 * An interface used to create subquery builders for expressions with multiple subqueries.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface MultipleSubqueryInitiator<T> {

    /**
     * Starts a {@link SubqueryInitiator} for the given subquery alias.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<MultipleSubqueryInitiator<T>> with(String subqueryAlias);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the given subquery alias.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     */
    public SubqueryBuilder<MultipleSubqueryInitiator<T>> with(String subqueryAlias, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Finishes the subquery builder.
     *
     * @return The parent query builder
     */
    public T end();
}
