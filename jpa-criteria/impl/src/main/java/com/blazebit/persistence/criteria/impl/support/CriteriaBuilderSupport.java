/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.support;

import jakarta.persistence.criteria.CollectionJoin;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.ListJoin;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.SetJoin;

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
