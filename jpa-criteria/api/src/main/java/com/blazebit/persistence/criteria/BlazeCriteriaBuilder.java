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

package com.blazebit.persistence.criteria;

import javax.persistence.Tuple;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import java.util.Map;

/**
 * An extended version of {@link CriteriaBuilder}.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeCriteriaBuilder extends CriteriaBuilder {

    /**
     * Create a predicate that tests whether a map is empty.
     *
     * @param map expression
     * @param <C> map type
     * @return is-empty predicate
     */
    public <C extends Map<?, ?>> Predicate isMapEmpty(Expression<C> map);

    /**
     * Create a predicate that tests whether a map is not empty.
     *
     * @param map expression
     * @param <C> map type
     * @return is-not-empty predicate
     */
    public <C extends Map<?, ?>> Predicate isMapNotEmpty(Expression<C> map);

    /**
     * Create an expression that tests the size of a map.
     *
     * @param map map
     * @param <C> map type
     * @return size expression
     */
    public <C extends Map<?, ?>> Expression<Integer> mapSize(Expression<C> map);

    /**
     * Create an expression that tests the size of a map.
     *
     * @param map map
     * @param <C> map type
     * @return size expression
     */
    public <C extends Map<?, ?>> Expression<Integer> mapSize(C map);

    /**
     * Like {@link CriteriaBuilder#asc(Expression)} but allows to also specify the null precedence.
     *
     * @param x          The expression used to define the ordering
     * @param nullsFirst True if nulls should be first, false otherwise
     * @return ascending ordering corresponding to the expression
     */
    public BlazeOrder asc(Expression<?> x, boolean nullsFirst);

    /**
     * Like {@link CriteriaBuilder#desc(Expression)} but allows to also specify the null precedence.
     *
     * @param x          The expression used to define the ordering
     * @param nullsFirst True if nulls should be first, false otherwise
     * @return descending ordering corresponding to the expression
     */
    public BlazeOrder desc(Expression<?> x, boolean nullsFirst);

    /**
     * Like {@link BlazeCriteriaBuilder#createCriteriaUpdate(Class)} but also sets the alias for the entity.
     *
     * @param targetEntity target type for update operation
     * @param alias        The alias for the entity
     * @param <T>          The type of the entity
     * @return the query object
     */
    public <T> BlazeCriteriaUpdate<T> createCriteriaUpdate(Class<T> targetEntity, String alias);

    /**
     * Like {@link BlazeCriteriaBuilder#createCriteriaDelete(Class)} but also sets the alias for the entity.
     *
     * @param targetEntity target type for delete operation
     * @param alias        The alias for the entity
     * @param <T>          The type of the entity
     * @return the query object
     */
    public <T> BlazeCriteriaDelete<T> createCriteriaDelete(Class<T> targetEntity, String alias);

    /* covariant overrides */

    @Override
    public BlazeOrder asc(Expression<?> x);

    @Override
    public BlazeOrder desc(Expression<?> x);

    @Override
    public BlazeCriteriaQuery<Object> createQuery();

    @Override
    public <T> BlazeCriteriaQuery<T> createQuery(Class<T> resultClass);

    @Override
    public BlazeCriteriaQuery<Tuple> createTupleQuery();

    /* Compatibility for JPA 2.1 */

    /**
     * Create a query object to perform a bulk update operation.
     *
     * @param targetEntity target type for update operation
     * @param <T>          The type of the entity
     * @return the query object
     */
    public <T> BlazeCriteriaUpdate<T> createCriteriaUpdate(Class<T> targetEntity);

    /**
     * Create a query object to perform a bulk delete operation.
     *
     * @param targetEntity target type for delete operation
     * @param <T>          The type of the entity
     * @return the query object
     */
    public <T> BlazeCriteriaDelete<T> createCriteriaDelete(Class<T> targetEntity);

    /**
     * Downcast Join object to the specified type.
     *
     * @param join Join object
     * @param type type to be downcast to
     * @param <X>  The source type
     * @param <T>  The type of the joined relation
     * @param <V>  The target treat type
     * @return Join object of the specified type
     */
    public <X, T, V extends T> BlazeJoin<X, V> treat(Join<X, T> join, Class<V> type);

    /**
     * Downcast CollectionJoin object to the specified type.
     *
     * @param join CollectionJoin object
     * @param type type to be downcast to
     * @param <X>  The source type
     * @param <T>  The type of the joined relation
     * @param <E>  The target treat type
     * @return CollectionJoin object of the specified type
     */
    public <X, T, E extends T> BlazeCollectionJoin<X, E> treat(CollectionJoin<X, T> join, Class<E> type);

    /**
     * Downcast SetJoin object to the specified type.
     *
     * @param join SetJoin object
     * @param type type to be downcast to
     * @param <X>  The source type
     * @param <T>  The type of the joined relation
     * @param <E>  The target treat type
     * @return SetJoin object of the specified type
     */
    public <X, T, E extends T> BlazeSetJoin<X, E> treat(SetJoin<X, T> join, Class<E> type);

    /**
     * Downcast ListJoin object to the specified type.
     *
     * @param join ListJoin object
     * @param type type to be downcast to
     * @param <X>  The source type
     * @param <T>  The type of the joined relation
     * @param <E>  The target treat type
     * @return ListJoin object of the specified type
     */
    public <X, T, E extends T> BlazeListJoin<X, E> treat(ListJoin<X, T> join, Class<E> type);

    /**
     * Downcast MapJoin object to the specified type.
     *
     * @param join MapJoin object
     * @param type type to be downcast to
     * @param <X>  The source type
     * @param <T>  The type of the joined relation
     * @param <K>  The key type of the joined relation
     * @param <V>  The target treat type
     * @return MapJoin object of the specified type
     */
    public <X, K, T, V extends T> BlazeMapJoin<X, K, V> treat(MapJoin<X, K, T> join, Class<V> type);

    /**
     * Downcast Path object to the specified type.
     *
     * @param path path
     * @param type type to be downcast to
     * @param <X>  The path type
     * @param <T>  The target treat type
     * @return Path object of the specified type
     */
    public <X, T extends X> BlazePath<T> treat(Path<X> path, Class<T> type);

    /**
     * Downcast Root object to the specified type.
     *
     * @param root root
     * @param type type to be downcast to
     * @param <X>  The root type
     * @param <T>  The target treat type
     * @return Path object of the specified type
     */
    public <X, T extends X> BlazeRoot<T> treat(Root<X> root, Class<T> type);

    /**
     * Downcast Join object to the specified type.
     *
     * @param join Join object
     * @param type type to be downcast to
     * @param <X>  The source type
     * @param <T>  The type of the joined relation
     * @param <V>  The target treat type
     * @return Join object of the specified type
     */
    public <X, T, V extends T> BlazeJoin<X, V> treat(BlazeJoin<X, T> join, Class<V> type);

    /**
     * Downcast CollectionJoin object to the specified type.
     *
     * @param join CollectionJoin object
     * @param type type to be downcast to
     * @param <X>  The source type
     * @param <T>  The type of the joined relation
     * @param <E>  The target treat type
     * @return CollectionJoin object of the specified type
     */
    public <X, T, E extends T> BlazeCollectionJoin<X, E> treat(BlazeCollectionJoin<X, T> join, Class<E> type);

    /**
     * Downcast SetJoin object to the specified type.
     *
     * @param join SetJoin object
     * @param type type to be downcast to
     * @param <X>  The source type
     * @param <T>  The type of the joined relation
     * @param <E>  The target treat type
     * @return SetJoin object of the specified type
     */
    public <X, T, E extends T> BlazeSetJoin<X, E> treat(BlazeSetJoin<X, T> join, Class<E> type);

    /**
     * Downcast ListJoin object to the specified type.
     *
     * @param join ListJoin object
     * @param type type to be downcast to
     * @param <X>  The source type
     * @param <T>  The type of the joined relation
     * @param <E>  The target treat type
     * @return ListJoin object of the specified type
     */
    public <X, T, E extends T> BlazeListJoin<X, E> treat(BlazeListJoin<X, T> join, Class<E> type);

    /**
     * Downcast MapJoin object to the specified type.
     *
     * @param join MapJoin object
     * @param type type to be downcast to
     * @param <X>  The source type
     * @param <T>  The type of the joined relation
     * @param <K>  The key type of the joined relation
     * @param <V>  The target treat type
     * @return MapJoin object of the specified type
     */
    public <X, K, T, V extends T> BlazeMapJoin<X, K, V> treat(BlazeMapJoin<X, K, T> join, Class<V> type);

    /**
     * Downcast Path object to the specified type.
     *
     * @param path path
     * @param type type to be downcast to
     * @param <X>  The path type
     * @param <T>  The target treat type
     * @return Path object of the specified type
     */
    public <X, T extends X> BlazePath<T> treat(BlazePath<X> path, Class<T> type);

    /**
     * Downcast Root object to the specified type.
     *
     * @param root root
     * @param type type to be downcast to
     * @param <X>  The root type
     * @param <T>  The target treat type
     * @return Path object of the specified type
     */
    public <X, T extends X> BlazeRoot<T> treat(BlazeRoot<X> root, Class<T> type);
}
