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

/**
 * A builder for criteria queries. This is the entry point for building queries.
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface CriteriaBuilder<T> extends FullQueryBuilder<T, CriteriaBuilder<T>>, BaseCriteriaBuilder<T, CriteriaBuilder<T>>, CTEBuilder<CriteriaBuilder<T>>, SetOperationBuilder<LeafOngoingSetOperationCriteriaBuilder<T>, StartOngoingSetOperationCriteriaBuilder<T, LeafOngoingFinalSetOperationCriteriaBuilder<T>>> {

    /**
     * Returns a query that counts the distinct query root results that would be produced if the current query was run.
     *
     * @return A query for determining the count of the distinct query root result list represented by this query builder
     * @since 1.3.0
     */
    public TypedQuery<Long> getQueryRootCountQuery();

    /**
     * Returns the query string that selects the distinct count of query root elements.
     *
     * @return The query string
     * @since 1.3.0
     */
    public String getQueryRootCountQueryString();

    @Override
    public <Y> CriteriaBuilder<Y> copy(Class<Y> resultClass);

    @Override
    public <Y> SelectObjectBuilder<CriteriaBuilder<Y>> selectNew(Class<Y> clazz);

    @Override
    public <Y> CriteriaBuilder<Y> selectNew(ObjectBuilder<Y> builder);
}
