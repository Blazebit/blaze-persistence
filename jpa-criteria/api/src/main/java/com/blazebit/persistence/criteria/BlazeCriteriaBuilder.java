package com.blazebit.persistence.criteria;

import javax.persistence.Tuple;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

public interface BlazeCriteriaBuilder extends CriteriaBuilder {

    public <T> Expression<T> functionFunction(String name, Class<T> returnType, Expression<?>... arguments);

    public BlazeOrder asc(Expression<?> x, boolean nullsFirst);

    public BlazeOrder desc(Expression<?> x, boolean nullsFirst);

    public <T> BlazeCriteriaUpdate<T> createCriteriaUpdate(Class<T> targetEntity, String alias);

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
     *
     * @return the query object
     */
    public <T> BlazeCriteriaUpdate<T> createCriteriaUpdate(Class<T> targetEntity);

    /**
     * Create a query object to perform a bulk delete operation.
     *
     * @param targetEntity target type for delete operation
     *
     * @return the query object
     */
    public <T> BlazeCriteriaDelete<T> createCriteriaDelete(Class<T> targetEntity);

    /**
     * Downcast Join object to the specified type.
     *
     * @param join Join object
     * @param type type to be downcast to
     *
     * @return Join object of the specified type
     */
    public <X, T, V extends T> BlazeJoin<X, V> treat(Join<X, T> join, Class<V> type);

    /**
     * Downcast CollectionJoin object to the specified type.
     *
     * @param join CollectionJoin object
     * @param type type to be downcast to
     *
     * @return CollectionJoin object of the specified type
     */
    public <X, T, E extends T> BlazeCollectionJoin<X, E> treat(CollectionJoin<X, T> join, Class<E> type);

    /**
     * Downcast SetJoin object to the specified type.
     *
     * @param join SetJoin object
     * @param type type to be downcast to
     *
     * @return SetJoin object of the specified type
     */
    public <X, T, E extends T> BlazeSetJoin<X, E> treat(SetJoin<X, T> join, Class<E> type);

    /**
     * Downcast ListJoin object to the specified type.
     *
     * @param join ListJoin object
     * @param type type to be downcast to
     *
     * @return ListJoin object of the specified type
     */
    public <X, T, E extends T> BlazeListJoin<X, E> treat(ListJoin<X, T> join, Class<E> type);

    /**
     * Downcast MapJoin object to the specified type.
     *
     * @param join MapJoin object
     * @param type type to be downcast to
     *
     * @return MapJoin object of the specified type
     */
    public <X, K, T, V extends T> BlazeMapJoin<X, K, V> treat(MapJoin<X, K, T> join, Class<V> type);

    /**
     * Downcast Path object to the specified type.
     *
     * @param path path
     * @param type type to be downcast to
     *
     * @return Path object of the specified type
     */
    public <X, T extends X> Path<T> treat(Path<X> path, Class<T> type);

    /**
     * Downcast Root object to the specified type.
     *
     * @param root root
     * @param type type to be downcast to
     *
     * @return Path object of the specified type
     */
    public <X, T extends X> BlazeRoot<T> treat(Root<X> root, Class<T> type);
}
