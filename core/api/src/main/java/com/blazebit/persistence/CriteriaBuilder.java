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
 * A builder for criteria queries. This is the entry point for building queries.
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface CriteriaBuilder<T> extends FullQueryBuilder<T, CriteriaBuilder<T>>, BaseCriteriaBuilder<T, CriteriaBuilder<T>>, CTEBuilder<CriteriaBuilder<T>>, SetOperationBuilder<LeafOngoingSetOperationCriteriaBuilder<T>, StartOngoingSetOperationCriteriaBuilder<T, LeafOngoingFinalSetOperationCriteriaBuilder<T>>> {

    @Override
    public <Y> CriteriaBuilder<Y> copy(Class<Y> resultClass);

    @Override
    public <Y> SelectObjectBuilder<CriteriaBuilder<Y>> selectNew(Class<Y> clazz);

    @Override
    public <Y> CriteriaBuilder<Y> selectNew(ObjectBuilder<Y> builder);
}
