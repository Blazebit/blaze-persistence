/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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

    /**
     * Returns a new window to be used with window functions.
     *
     * @return the new window
     */
    public BlazeWindow window();

    /**
     * Creates a function expression for a function with the given name, result type and arguments.
     *
     * @param name the name of the function
     * @param type the result type of function
     * @param args the arguments for the function
     * @param <T> the result type
     * @return the function expression
     */
    @Override
    public <T> BlazeFunctionExpression<T> function(String name, Class<T> type, Expression<?>... args);

    /**
     * Creates a window function expression for a function with the given name, result type and arguments.
     *
     * @param name the name of the function
     * @param type the result type of function
     * @param args the arguments for the function
     * @param <T> the result type
     * @return the window function expression
     */
    public <T> BlazeWindowFunctionExpression<T> windowFunction(String name, Class<T> type, Expression<?>... args);

    /**
     * Creates a window function expression for a function with the given name, result type and arguments aggregating only distinct elements.
     *
     * @param name the name of the function
     * @param type the result type of function
     * @param args the arguments for the function
     * @param <T> the result type
     * @return the window function expression
     */
    public <T> BlazeWindowFunctionExpression<T> windowDistinctFunction(String name, Class<T> type, Expression<?>... args);

    /**
     * Creates an aggregate function expression for a function with the given name, result type and arguments.
     *
     * @param name the name of the function
     * @param type the result type of function
     * @param args the arguments for the function
     * @param <T> the result type
     * @return the aggregate function expression
     */
    public <T> BlazeAggregateFunctionExpression<T> aggregateFunction(String name, Class<T> type, Expression<?>... args);

    /**
     * Creates an aggregate function expression for a function with the given name, result type and arguments aggregating only distinct elements.
     *
     * @param name the name of the function
     * @param type the result type of function
     * @param args the arguments for the function
     * @param <T> the result type
     * @return the aggregate function expression
     */
    public <T> BlazeAggregateFunctionExpression<T> aggregateDistinctFunction(String name, Class<T> type, Expression<?>... args);

    /**
     * Creates an ordered set-aggregate function expression for a function with the given name, result type and arguments.
     *
     * @param name the name of the function
     * @param type the result type of function
     * @param args the arguments for the function
     * @param <T> the result type
     * @return the ordered set-aggregate function expression
     */
    public <T> BlazeOrderedSetAggregateFunctionExpression<T> orderedSetAggregateFunction(String name, Class<T> type, Expression<?>... args);

    /**
     * Creates an ordered set-aggregate function expression for a function with the given name, result type and arguments aggregating only distinct elements.
     *
     * @param name the name of the function
     * @param type the result type of function
     * @param args the arguments for the function
     * @param <T> the result type
     * @return the ordered set-aggregate function expression
     */
    public <T> BlazeOrderedSetAggregateFunctionExpression<T> orderedSetAggregateDistinctFunction(String name, Class<T> type, Expression<?>... args);

    /**
     * Creates an <code>AVG</code> aggregate function expression.
     *
     * @param x the argument to aggregate
     * @param <N> The number type
     * @return the aggregate function expression
     */
    @Override
    public <N extends Number> BlazeAggregateFunctionExpression<Double> avg(Expression<N> x);

    /**
     * Creates a <code>SUM</code> aggregate function expression.
     *
     * @param x the argument to aggregate
     * @param <N> The number type
     * @return the aggregate function expression
     */
    @Override
    public <N extends Number> BlazeAggregateFunctionExpression<N> sum(Expression<N> x);

    /**
     * Creates a <code>SUM</code> aggregate function expression that returns a long value.
     *
     * @param x the argument to aggregate
     * @return the aggregate function expression
     */
    @Override
    public BlazeAggregateFunctionExpression<Long> sumAsLong(Expression<Integer> x);

    /**
     * Creates a <code>SUM</code> aggregate function expression that returns a double value.
     *
     * @param x the argument to aggregate
     * @return the aggregate function expression
     */
    @Override
    public BlazeAggregateFunctionExpression<Double> sumAsDouble(Expression<Float> x);

    /**
     * Creates a <code>MAX</code> aggregate function expression.
     *
     * @param x the argument to aggregate
     * @param <N> The number type
     * @return the aggregate function expression
     */
    @Override
    public <N extends Number> BlazeAggregateFunctionExpression<N> max(Expression<N> x);

    /**
     * Creates a <code>MIN</code> aggregate function expression.
     *
     * @param x the argument to aggregate
     * @param <N> The number type
     * @return the aggregate function expression
     */
    @Override
    public <N extends Number> BlazeAggregateFunctionExpression<N> min(Expression<N> x);

    /**
     * Creates a <code>MAX</code> aggregate function expression.
     *
     * @param x the argument to aggregate
     * @param <X> The argument type
     * @return the aggregate function expression
     */
    @Override
    public <X extends Comparable<? super X>> BlazeAggregateFunctionExpression<X> greatest(Expression<X> x);

    /**
     * Creates a <code>MIN</code> aggregate function expression.
     *
     * @param x the argument to aggregate
     * @param <X> The argument type
     * @return the aggregate function expression
     */
    @Override
    public <X extends Comparable<? super X>> BlazeAggregateFunctionExpression<X> least(Expression<X> x);

    /**
     * Creates a <code>COUNT</code> aggregate function expression.
     *
     * @param x the argument to use for determining whether to count
     * @return the aggregate function expression
     */
    @Override
    public BlazeAggregateFunctionExpression<Long> count(Expression<?> x);

    /**
     * Creates a <code>COUNT</code> aggregate function expression which only counts distinct elements.
     *
     * @param x the argument to use for determining whether to count
     * @return the aggregate function expression
     */
    @Override
    public BlazeAggregateFunctionExpression<Long> countDistinct(Expression<?> x);

    /**
     * Creates a <code>ROW_NUMBER</code> window function expression.
     *
     * @return the window function expression
     */
    public BlazeWindowFunctionExpression<Integer> rowNumber();

    /**
     * Creates a <code>RANK</code> window function expression.
     *
     * @param expression the expression for which to apply the window function to
     * @return the window function expression
     */
    public BlazeWindowFunctionExpression<Integer> rank(Expression<?> expression);

    /**
     * Creates a <code>DENSE_RANK</code> window function expression.
     *
     * @param expression the expression for which to apply the window function to
     * @return the window function expression
     */
    public BlazeWindowFunctionExpression<Integer> denseRank(Expression<?> expression);

    /**
     * Creates a <code>PERCENT_RANK</code> window function expression.
     *
     * @param expression the expression for which to apply the window function to
     * @return the window function expression
     */
    public BlazeWindowFunctionExpression<Double> percentRank(Expression<?> expression);

    /**
     * Creates a <code>CUME_DIST</code> window function expression.
     *
     * @param expression the expression for which to apply the window function to
     * @return the window function expression
     */
    public BlazeWindowFunctionExpression<Double> cumeDist(Expression<?> expression);

    /**
     * Creates a <code>NTILE</code> window function expression.
     *
     * @param expression the expression for which to apply the window function to
     * @return the window function expression
     */
    public BlazeWindowFunctionExpression<Integer> ntile(Expression<?> expression);

    /**
     * Creates a <code>LEAD</code> window function expression.
     *
     * @param expression the expression for which to apply the window function to
     * @param <X> The expression type
     * @return the window function expression
     */
    public <X> BlazeWindowFunctionExpression<X> lead(Expression<X> expression);

    /**
     * Creates a <code>LAG</code> window function expression.
     *
     * @param expression the expression for which to apply the window function to
     * @param <X> The expression type
     * @return the window function expression
     */
    public <X> BlazeWindowFunctionExpression<X> lag(Expression<X> expression);

    /**
     * Creates a <code>FIRST_VALUE</code> window function expression.
     *
     * @param expression the expression for which to apply the window function to
     * @param <X> The expression type
     * @return the window function expression
     */
    public <X> BlazeWindowFunctionExpression<X> firstValue(Expression<X> expression);

    /**
     * Creates a <code>LAST_VALUE</code> window function expression.
     *
     * @param expression the expression for which to apply the window function to
     * @param <X> The expression type
     * @return the window function expression
     */
    public <X> BlazeWindowFunctionExpression<X> lastValue(Expression<X> expression);

    /**
     * Creates a <code>NTH_VALUE</code> window function expression.
     *
     * @param expression the expression for which to apply the window function to
     * @param index the index of the value relative to the frame start
     * @param <X> The expression type
     * @return the window function expression
     */
    public <X> BlazeWindowFunctionExpression<X> nthValue(Expression<X> expression, Expression<Integer> index);

    /**
     * Creates a <code>PERCENTILE_CONT</code> ordered set-aggregate function expression.
     *
     * @param fraction the fraction of the ordering for which to return a value for
     * @param group the group within which to determine the percentile
     * @param ascending Whether to sort by group ascending or descending
     * @param nullsFirst Whether to sort nulls of the group first or last
     * @param <X> The expression type
     * @return the ordered set-aggregate function expression
     */
    public <X> BlazeOrderedSetAggregateFunctionExpression<X> percentileContWithinGroup(Expression<Double> fraction, Expression<X> group, boolean ascending, boolean nullsFirst);

    /**
     * Creates a <code>PERCENTILE_DISC</code> ordered set-aggregate function expression.
     *
     * @param fraction the fraction of the ordering for which to return a value for
     * @param group the group within which to determine the percentile
     * @param ascending Whether to sort by group ascending or descending
     * @param nullsFirst Whether to sort nulls of the group first or last
     * @param <X> The expression type
     * @return the ordered set-aggregate function expression
     */
    public <X> BlazeOrderedSetAggregateFunctionExpression<X> percentileDiscWithinGroup(Expression<Double> fraction, Expression<X> group, boolean ascending, boolean nullsFirst);

    /**
     * Creates a <code>MODE</code> ordered set-aggregate function expression.
     *
     * @param group the group within which to find the mode
     * @param <X> The expression type
     * @return the ordered set-aggregate function expression
     */
    public <X> BlazeOrderedSetAggregateFunctionExpression<X> modeWithinGroup(Expression<X> group);

    /**
     * Creates a <code>LISTAGG</code> ordered set-aggregate function expression.
     *
     * @param expression the argument to list aggregate
     * @param separator the separator to put between elements in the aggregation
     * @return the ordered set-aggregate function expression
     */
    public BlazeOrderedSetAggregateFunctionExpression<String> listagg(Expression<String> expression, Expression<String> separator);

    /**
     * Creates a <code>LISTAGG</code> ordered set-aggregate function expression which only aggregates distinct elements.
     *
     * @param expression the argument to list aggregate
     * @param separator the separator to put between elements in the aggregation
     * @return the ordered set-aggregate function expression
     */
    public BlazeOrderedSetAggregateFunctionExpression<String> listaggDistinct(Expression<String> expression, Expression<String> separator);
}
