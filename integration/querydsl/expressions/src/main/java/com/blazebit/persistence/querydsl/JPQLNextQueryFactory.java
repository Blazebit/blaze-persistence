/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.jpa.JPQLQueryFactory;

import java.util.Collection;
import java.util.List;

/**
 * Query factory to simplify {@link BlazeJPAQuery} instantiation.
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.6.2
 */
public interface JPQLNextQueryFactory extends JPQLQueryFactory {

    @Override
    <T> JPQLNextQuery<T> select(Expression<T> expr);

    @Override
    JPQLNextQuery<Tuple> select(Expression<?>... exprs);

    @Override
    <T> JPQLNextQuery<T> selectDistinct(Expression<T> expr);

    @Override
    JPQLNextQuery<Tuple> selectDistinct(Expression<?>... exprs);

    @Override
    JPQLNextQuery<Integer> selectOne();

    @Override
    JPQLNextQuery<Integer> selectZero();

    @Override
    <T> JPQLNextQuery<T> selectFrom(EntityPath<T> from);

    @Override
    JPQLNextQuery<?> from(EntityPath<?> from);

    @Override
    JPQLNextQuery<?> from(EntityPath<?>... from);

    /**
     * Create a new Query with the given source
     *
     * @param subQueryExpression The subquery expression
     * @param alias Alias for the subquery in the outer query
     * @return from(from)
     */
    <X> JPQLNextQuery<?> from(SubQueryExpression<X> subQueryExpression, Path<X> alias);

    /**
     * Create a new Query with the given source
     *
     * @param subQueryExpression The subquery expression
     * @param alias Alias for the subquery in the outer query
     * @return from(from)
     */
    <X> JPQLNextQuery<X> selectFrom(SubQueryExpression<X> subQueryExpression, Path<X> alias);

    /**
     * Select from a set of values using the {@code VALUES} clause.
     *
     * @param path Type of values
     * @param elements The elements
     * @param <X> The element type
     * @return this query
     */
    <X> JPQLNextQuery<?> fromValues(EntityPath<X> path, Collection<X> elements);

    /**
     * Select from a set of values using the {@code VALUES} clause.
     *
     * @param path Type of values
     * @param elements The elements
     * @param <X> The element type
     * @return this query
     */
    <X> JPQLNextQuery<?> fromIdentifiableValues(EntityPath<X> path, Collection<X> elements);

    /**
     * Select from a set of values using the {@code VALUES} clause.
     *
     * @param path Type of values
     * @param alias The alias from which the values can be referenced
     * @param elements The elements
     * @param <X> The element type
     * @return this query
     */
    <X> JPQLNextQuery<?> fromValues(Path<X> path, Path<X> alias, Collection<X> elements);

    /**
     * Select from a set of values using the {@code VALUES} clause.
     *
     * @param path Type of values
     * @param alias The alias from which the values can be referenced
     * @param elements The elements
     * @param <X> The element type
     * @return this query
     */
    <X> JPQLNextQuery<?> fromIdentifiableValues(Path<X> path, Path<X> alias, Collection<X> elements);

    /**
     * Register a common table expression (CTE).
     *
     * @param alias The alias for the CTE
     * @param o The subquery expression
     * @param <X> CTE type
     * @return this query
     */
    <X> JPQLNextQuery<?> with(Path<X> alias, SubQueryExpression<?> o);

    /**
     * Register a recursive common table expression (CTE).
     *
     * @param alias The alias for the CTE
     * @param o The subquery expression
     * @param <X> CTE type
     * @return this query
     */
    <X> JPQLNextQuery<?> withRecursive(Path<X> alias, SubQueryExpression<?> o);

    /**
     * Register a common table expression (CTE). Returns a builder through which
     * the CTE can be provided as {@link SubQueryExpression}.
     *
     * @apiNote This version does not allow for set operands to use different column bindings.
     *  For that purpose, use {@link #with(Path, SubQueryExpression)} instead, and wrap each
     *  select expression inside a {@link JPQLNextOps#BIND} operation.
     * @param alias The alias for the CTE
     * @param columns The columns for the CTE
     * @return this query
     */
    WithBuilder<? extends JPQLNextQuery<?>> with(EntityPath<?> alias, Path<?>... columns);

    /**
     * Register a recursive common table expression (CTE). Returns a builder through which
     * the CTE can be provided as {@link SubQueryExpression}.
     *
     * @apiNote This version does not allow for set operands to use different column bindings.
     *  For that purpose, use {@link #with(Path, SubQueryExpression)} instead, and wrap each
     *  select expression inside a {@link JPQLNextOps#BIND} operation.
     * @param alias The alias for the CTE
     * @param columns The columns for the CTE
     * @return this query
     */
    WithBuilder<? extends JPQLNextQuery<?>> withRecursive(EntityPath<?> alias, Path<?>... columns);

    @Override
    JPQLNextQuery<?> query();

    /**
     * Creates an union expression for the given subqueries.
     *
     * @param <RT> set operation type
     * @param sq subqueries
     * @return the set operation result
     */
    <RT> SetExpression<RT> union(List<SubQueryExpression<RT>> sq);

    /**
     * Creates an union expression for the given subqueries.
     *
     * @param <RT> set operation type
     * @param sq subqueries
     * @return the set operation result
     */
    <RT> SetExpression<RT> unionAll(List<SubQueryExpression<RT>> sq);

    /**
     * Creates an intersect expression for the given subqueries.
     *
     * @param <RT> set operation type
     * @param sq subqueries
     * @return the set operation result
     * @see #union(List)
     */
    <RT> SetExpression<RT> intersect(List<SubQueryExpression<RT>> sq);

    /**
     * Creates an intersect expression for the given subqueries
     *
     * @param <RT> set operation type
     * @param sq subqueries
     * @return the set operation result
     * @see #union(List)
     */
    <RT> SetExpression<RT> intersectAll(List<SubQueryExpression<RT>> sq);

    /**
     * Creates an except expression for the given subqueries
     *
     * @param <RT> set operation type
     * @param sq subqueries
     * @return the set operation result
     * @see #union(List)
     */
    <RT> SetExpression<RT> except(List<SubQueryExpression<RT>> sq);

    /**
     * Creates an except expression for the given subqueries
     *
     * @param <RT> set operation type
     * @param sq subqueries
     * @return the set operation result
     * @see #union(List)
     */
    <RT> SetExpression<RT> exceptAll(List<SubQueryExpression<RT>> sq);

    /**
     * Creates an union expression for the given subqueries.
     *
     * @param <RT> set operation type
     * @param sq subqueries
     * @return the set operation result
     */
    @SuppressWarnings("unsafe")
    <RT> SetExpression<RT> union(SubQueryExpression<RT>... sq);

    /**
     * Creates an union expression for the given subqueries.
     *
     * @param <RT> set operation type
     * @param sq subqueries
     * @return the set operation result
     */
    @SuppressWarnings("unsafe")
    <RT> SetExpression<RT> unionAll(SubQueryExpression<RT>... sq);

    /**
     * Creates an intersect expression for the given subqueries.
     *
     * @param <RT> set operation type
     * @param sq subqueries
     * @return the set operation result
     */
    @SuppressWarnings("unsafe")
    <RT> SetExpression<RT> intersect(SubQueryExpression<RT>... sq);

    /**
     * Creates an intersect expression for the given subqueries.
     *
     * @param <RT> set operation type
     * @param sq subqueries
     * @return the set operation result
     */
    @SuppressWarnings("unsafe")
    <RT> SetExpression<RT> intersectAll(SubQueryExpression<RT>... sq);

    /**
     * Creates an except expression for the given subqueries.
     *
     * @param <RT> set operation type
     * @param sq subqueries
     * @return the set operation result
     */
    @SuppressWarnings("unsafe")
    <RT> SetExpression<RT> except(SubQueryExpression<RT>... sq);

    /**
     * Creates an except expression for the given subqueries.
     *
     * @param <RT> set operation type
     * @param sq subqueries
     * @return the set operation result
     */
    @SuppressWarnings("unsafe")
    <RT> SetExpression<RT> exceptAll(SubQueryExpression<RT>... sq);

}
