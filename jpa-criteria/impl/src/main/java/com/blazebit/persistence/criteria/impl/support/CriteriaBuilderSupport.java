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

package com.blazebit.persistence.criteria.impl.support;

import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

/**
 * An interface necessary to be able to compile against JPA 2.0 but let users use JPA 2.1 APIs.
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public interface CriteriaBuilderSupport extends CriteriaBuilder {

    public <T> CriteriaUpdate<T> createCriteriaUpdate(Class<T> targetEntity);

    public <T> CriteriaDelete<T> createCriteriaDelete(Class<T> targetEntity);

    public <X, T, V extends T> Join<X, V> treat(Join<X, T> join, Class<V> type);

    public <X, T, E extends T> CollectionJoin<X, E> treat(CollectionJoin<X, T> join, Class<E> type);

    public <X, T, E extends T> SetJoin<X, E> treat(SetJoin<X, T> join, Class<E> type);

    public <X, T, E extends T> ListJoin<X, E> treat(ListJoin<X, T> join, Class<E> type);

    public <X, K, T, V extends T> MapJoin<X, K, V> treat(MapJoin<X, K, T> join, Class<V> type);

    public <X, T extends X> Path<T> treat(Path<X> path, Class<T> type);

    public <X, T extends X> Root<T> treat(Root<X> root, Class<T> type);
}
