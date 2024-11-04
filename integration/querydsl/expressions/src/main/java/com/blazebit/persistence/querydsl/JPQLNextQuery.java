/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.querydsl;

import com.querydsl.core.QueryModifiers;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.CollectionExpression;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.MapExpression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.jpa.JPQLQuery;

import java.util.Collection;
import java.util.List;

/**
 * Query interface for JPQL.Next queries
 *
 * @param <T> Query result type
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
@SuppressWarnings("unused")
public interface JPQLNextQuery<T> extends JPQLQuery<T>, ExtendedFetchable<T> {

    /**
     * Register a common table expression (CTE).
     * Analog to {@link com.querydsl.sql.ProjectableSQLQuery#with(Path, SubQueryExpression)}.
     *
     * @param alias The alias for the CTE
     * @param o The subquery expression
     * @param <X> CTE type
     * @return this query
     */
    <X> JPQLNextQuery<T> with(Path<X> alias, SubQueryExpression<?> o);

    /**
     * Register a recursive common table expression (CTE).
     * Analog to {@link com.querydsl.sql.ProjectableSQLQuery#withRecursive(Path, SubQueryExpression)}.
     *
     * @param alias The alias for the CTE
     * @param o The subquery expression
     * @param <X> CTE type
     * @return this query
     */
    <X> JPQLNextQuery<T> withRecursive(Path<X> alias, SubQueryExpression<?> o);

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
    WithBuilder<? extends JPQLNextQuery<T>> with(EntityPath<?> alias, Path<?>... columns);

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
    WithBuilder<? extends JPQLNextQuery<T>> withRecursive(EntityPath<?> alias, Path<?>... columns);

    /**
     * Select from a set of values using the {@code VALUES} clause.
     *
     * @param path Type of values
     * @param elements The elements
     * @param <X> The element type
     * @return this query
     */
    <X> JPQLNextQuery<T> fromValues(EntityPath<X> path, Collection<X> elements);

    /**
     * Select from a set of values using the {@code VALUES} clause.
     *
     * @param path Type of values
     * @param elements The elements
     * @param <X> The element type
     * @return this query
     */
    <X> JPQLNextQuery<T> fromIdentifiableValues(EntityPath<X> path, Collection<X> elements);

    /**
     * Select from a set of values using the {@code VALUES} clause.
     *
     * @param path Type of values
     * @param alias The alias from which the values can be referenced
     * @param elements The elements
     * @param <X> The element type
     * @return this query
     */
    <X> JPQLNextQuery<T> fromValues(Path<X> path, Path<X> alias, Collection<X> elements);

    /**
     * Select from a set of values using the {@code VALUES} clause.
     *
     * @param path Type of values
     * @param alias The alias from which the values can be referenced
     * @param elements The elements
     * @param <X> The element type
     * @return this query
     */
    <X> JPQLNextQuery<T> fromIdentifiableValues(Path<X> path, Path<X> alias, Collection<X> elements);

    /**
     * Create a full join with the given target.
     * Analog to {@link com.querydsl.sql.SQLCommonQuery#fullJoin(EntityPath)}.
     * Use {@link #fetchJoin()} to add the fetchJoin parameter to this join.
     * Use {@link #lateral()} to use a lateral join for this join.
     *
     * @param <P> The type of the join target
     * @param target The join target
     * @return this query
     * @apiNote Full joins are only supported by some ORMs, like Hibernate.
     */
    <P> JPQLNextQuery<T> fullJoin(CollectionExpression<?, P> target);

    /**
     * Create a full join with the given target.
     * Analog to {@link com.querydsl.sql.SQLCommonQuery#fullJoin(EntityPath)}.
     * Use {@link #fetchJoin()} to add the fetchJoin parameter to this join.
     * Use {@link #lateral()} to use a lateral join for this join.
     *
     * @param <P> The type of the join target
     * @param target The join target
     * @param alias alias
     * @return the current object
     * @apiNote Full joins are only supported by some ORMs, like Hibernate.
     */
    <P> JPQLNextQuery<T> fullJoin(CollectionExpression<?, P> target, Path<P> alias);

    /**
     * Create a full join with the given target.
     * Analog to {@link com.querydsl.sql.SQLCommonQuery#fullJoin(EntityPath)}.
     * Use {@link #fetchJoin()} to add the fetchJoin parameter to this join.
     * Use {@link #lateral()} to use a lateral join for this join.
     *
     * @param <P> The type of the join target
     * @param target The join target
     * @return the current object
     * @apiNote Full joins are only supported by some ORMs, like Hibernate.
     */
    <P> JPQLNextQuery<T> fullJoin(EntityPath<P> target);

    /**
     * Create a full join with the given target.
     * Analog to {@link com.querydsl.sql.SQLCommonQuery#fullJoin(EntityPath)}.
     * Use {@link #fetchJoin()} to add the fetchJoin parameter to this join.
     * Use {@link #lateral()} to use a lateral join for this join.
     *
     * @param <P> The type of the join target
     * @param target The join target
     * @param alias The alias under which the join can be referenced
     * @return the current object
     * @apiNote Full joins are only supported by some ORMs, like Hibernate.
     */
    <P> JPQLNextQuery<T> fullJoin(EntityPath<P> target, Path<P> alias);

    /**
     * Create a full join with the given target.
     * Analog to {@link com.querydsl.sql.SQLCommonQuery#fullJoin(EntityPath)}.
     * Use {@link #fetchJoin()} to add the fetchJoin parameter to this join.
     * Use {@link #lateral()} to use a lateral join for this join.
     *
     * @param <P> The type of the join target
     * @param target The join target
     * @return the current object
     * @apiNote Full joins are only supported by some ORMs, like Hibernate.
     */
    <P> JPQLNextQuery<T> fullJoin(MapExpression<?, P> target);

    /**
     * Create a full join with the given target.
     * Analog to {@link com.querydsl.sql.SQLCommonQuery#fullJoin(EntityPath)}.
     * Use {@link #fetchJoin()} to add the fetchJoin parameter to this join.
     * Use {@link #lateral()} to use a lateral join for this join.
     *
     * @param <P> The type of the join target
     * @param target The join target
     * @param alias The alias under which the join can be referenced
     * @return the current object
     * @apiNote Full joins are only supported by some ORMs, like Hibernate.
     */
    <P> JPQLNextQuery<T> fullJoin(MapExpression<?, P> target, Path<P> alias);

    /**
     * Add sources to this query
     *
     * @param subQueryExpression Subquery expression
     * @param alias The alias for the subquery from which it can be referenced in the outer query
     * @param <X> Expression type
     * @return the current object
     */
    <X> JPQLNextQuery<T> from(SubQueryExpression<X> subQueryExpression, Path<X> alias);

    /**
     * Adds a left join to the given subquery target.
     * Analog to {@link com.querydsl.sql.SQLCommonQuery#leftJoin(SubQueryExpression, Path)}.
     *
     * @param o subquery
     * @param alias alias
     * @param <X> Expression type
     * @return the current object
     */
    <X> JPQLNextQuery<T> leftJoin(SubQueryExpression<X> o, Path<X> alias);

    /**
     * Adds a right join to the given target.
     * Analog to {@link com.querydsl.sql.SQLCommonQuery#rightJoin(SubQueryExpression, Path)}.
     *
     * @param o subquery
     * @param alias alias
     * @param <X> Expression type
     * @return the current object
     */
    <X> JPQLNextQuery<T> rightJoin(SubQueryExpression<X> o, Path<X> alias);

    /**
     * Adds a full join to the given target.
     * Analog to {@link com.querydsl.sql.SQLCommonQuery#fullJoin(SubQueryExpression, Path)}.
     *
     * @param o subquery
     * @param alias alias
     * @param <X> Expression type
     * @return the current object
     */
    <X> JPQLNextQuery<T> fullJoin(SubQueryExpression<X> o, Path<X> alias);

    /**
     * Adds a inner join to the given target.
     * Analog to {@link com.querydsl.sql.SQLCommonQuery#innerJoin(SubQueryExpression, Path)}.
     *
     * @param o subquery
     * @param alias alias
     * @param <X> Expression type
     * @return the current object
     */
    <X> JPQLNextQuery<T> innerJoin(SubQueryExpression<X> o, Path<X> alias);

    /**
     * Creates an union expression for the given subqueries.
     * Analog to {@link com.querydsl.sql.ProjectableSQLQuery#union(List)}.
     *
     * @param <RT> set operation type
     * @param sq JPQLNextQuery subqueries
     * @return the set operation result
     */
    <RT> SetExpression<RT> union(List<SubQueryExpression<RT>> sq);

    /**
     * Creates an union expression for the given subqueries.
     * Analog to {@link com.querydsl.sql.ProjectableSQLQuery#unionAll(List)}.
     *
     * @param <RT> set operation type
     * @param sq JPQLNextQuery subqueries
     * @return the set operation result
     */
    <RT> SetExpression<RT> unionAll(List<SubQueryExpression<RT>> sq);

    /**
     * Creates an intersect expression for the given subqueries.
     *
     * @param <RT> set operation type
     * @param sq JPQLNextQuery subqueries
     * @return the set operation result
     * @see #union(List)
     */
    <RT> SetExpression<RT> intersect(List<SubQueryExpression<RT>> sq);

    /**
     * Creates an intersect expression for the given subqueries
     *
     * @param <RT> set operation type
     * @param sq JPQLNextQuery subqueries
     * @return the set operation result
     * @see #union(List)
     */
    <RT> SetExpression<RT> intersectAll(List<SubQueryExpression<RT>> sq);

    /**
     * Creates an except expression for the given subqueries
     *
     * @param <RT> set operation type
     * @param sq JPQLNextQuery subqueries
     * @return the set operation result
     * @see #union(List)
     */
    <RT> SetExpression<RT> except(List<SubQueryExpression<RT>> sq);

    /**
     * Creates an except expression for the given subqueries
     *
     * @param <RT> set operation type
     * @param sq JPQLNextQuery subqueries
     * @return the set operation result
     * @see #union(List)
     */
    <RT> SetExpression<RT> exceptAll(List<SubQueryExpression<RT>> sq);

    /**
     * Creates an union expression for the given subqueries.
     * Analog to {@link com.querydsl.sql.ProjectableSQLQuery#union(List)}.
     *
     * @param <RT> set operation type
     * @param sq JPQLNextQuery subqueries
     * @return the set operation result
     */
    @SuppressWarnings("unchecked")
    <RT> SetExpression<RT> union(SubQueryExpression<RT>... sq);

    /**
     * Creates an union expression for the given subqueries.
     * Analog to {@link com.querydsl.sql.ProjectableSQLQuery#unionAll(List)}.
     *
     * @param <RT> set operation type
     * @param sq JPQLNextQuery subqueries
     * @return the set operation result
     */
    @SuppressWarnings("unchecked")
    <RT> SetExpression<RT> unionAll(SubQueryExpression<RT>... sq);

    /**
     * Creates an intersect expression for the given subqueries.
     *
     * @param <RT> set operation type
     * @param sq JPQLNextQuery subqueries
     * @return the set operation result
     */
    @SuppressWarnings("unchecked")
    <RT> SetExpression<RT> intersect(SubQueryExpression<RT>... sq);

    /**
     * Creates an intersect expression for the given subqueries.
     *
     * @param <RT> set operation type
     * @param sq JPQLNextQuery subqueries
     * @return the set operation result
     */
    @SuppressWarnings("unchecked")
    <RT> SetExpression<RT> intersectAll(SubQueryExpression<RT>... sq);

    /**
     * Creates an except expression for the given subqueries.
     *
     * @param <RT> set operation type
     * @param sq JPQLNextQuery subqueries
     * @return the set operation result
     */
    @SuppressWarnings("unchecked")
    <RT> SetExpression<RT> except(SubQueryExpression<RT>... sq);

    /**
     * Creates an except expression for the given subqueries.
     *
     * @param <RT> set operation type
     * @param sq JPQLNextQuery subqueries
     * @return the set operation result
     */
    @SuppressWarnings("unchecked")
    <RT> SetExpression<RT> exceptAll(SubQueryExpression<RT>... sq);

    /**
     * Mark the last join as a lateral join.
     * 
     * @return this query
     */
    JPQLNextQuery<T> lateral();

    /**
     * Mark the last join as a default join.
     *
     * @return this query
     */
    JPQLNextQuery<T> defaultJoin();

    // Covariant Overrides

    @Override
    JPQLNextQuery<T> from(EntityPath<?>... sources);

    @Override
    <P> JPQLNextQuery<T> from(CollectionExpression<?, P> target, Path<P> alias);

    @Override
    <P> JPQLNextQuery<T> innerJoin(EntityPath<P> target);

    @Override
    <P> JPQLNextQuery<T> innerJoin(EntityPath<P> target, Path<P> alias);

    @Override
    <P> JPQLNextQuery<T> innerJoin(CollectionExpression<?, P> target);

    @Override
    <P> JPQLNextQuery<T> innerJoin(CollectionExpression<?, P> target, Path<P> alias);

    @Override
    <P> JPQLNextQuery<T> innerJoin(MapExpression<?, P> target);

    @Override
    <P> JPQLNextQuery<T> innerJoin(MapExpression<?, P> target, Path<P> alias);

    @Override
    <P> JPQLNextQuery<T> join(EntityPath<P> target);

    @Override
    <P> JPQLNextQuery<T> join(EntityPath<P> target, Path<P> alias);

    @Override
    <P> JPQLNextQuery<T> join(CollectionExpression<?, P> target);

    @Override
    <P> JPQLNextQuery<T> join(CollectionExpression<?, P> target, Path<P> alias);

    @Override
    <P> JPQLNextQuery<T> join(MapExpression<?, P> target);

    @Override
    <P> JPQLNextQuery<T> join(MapExpression<?, P> target, Path<P> alias);

    @Override
    <P> JPQLNextQuery<T> leftJoin(EntityPath<P> target);

    @Override
    <P> JPQLNextQuery<T> leftJoin(EntityPath<P> target, Path<P> alias);

    @Override
    <P> JPQLNextQuery<T> leftJoin(CollectionExpression<?, P> target);

    @Override
    <P> JPQLNextQuery<T> leftJoin(CollectionExpression<?, P> target, Path<P> alias);

    @Override
    <P> JPQLNextQuery<T> leftJoin(MapExpression<?, P> target);

    @Override
    <P> JPQLNextQuery<T> leftJoin(MapExpression<?, P> target, Path<P> alias);

    @Override
    <P> JPQLNextQuery<T> rightJoin(EntityPath<P> target);

    @Override
    <P> JPQLNextQuery<T> rightJoin(EntityPath<P> target, Path<P> alias);

    @Override
    <P> JPQLNextQuery<T> rightJoin(CollectionExpression<?, P> target);

    @Override
    <P> JPQLNextQuery<T> rightJoin(CollectionExpression<?, P> target, Path<P> alias);

    @Override
    <P> JPQLNextQuery<T> rightJoin(MapExpression<?, P> target);

    @Override
    <P> JPQLNextQuery<T> rightJoin(MapExpression<?, P> target, Path<P> alias);

    @Override
    JPQLNextQuery<T> on(Predicate... condition);

    @Override
    JPQLNextQuery<T> fetchJoin();

    @Override
    JPQLNextQuery<T> fetchAll();

    @Override
    JPQLNextQuery<T> groupBy(Expression<?>... expressions);

    @Override
    JPQLNextQuery<T> having(Predicate... predicates);

    @Override
    JPQLNextQuery<T> limit(long l);

    @Override
    JPQLNextQuery<T> offset(long l);

    @Override
    JPQLNextQuery<T> restrict(QueryModifiers queryModifiers);

    @Override
    JPQLNextQuery<T> orderBy(OrderSpecifier<?>... orderSpecifiers);

    @Override
    <U> JPQLNextQuery<T> set(ParamExpression<U> paramExpression, U t);

    @Override
    JPQLNextQuery<T> distinct();

    @Override
    JPQLNextQuery<T> where(Predicate... predicates);

    /**
     * Add a named window to this query.
     *
     * @param namedWindow The window definition to add
     * @return this query
     */
    JPQLNextQuery<T> window(NamedWindow namedWindow);

    @Override
    <U> JPQLNextQuery<U> select(Expression<U> expr);

    @Override
    JPQLNextQuery<Tuple> select(Expression<?>... exprs);
}
