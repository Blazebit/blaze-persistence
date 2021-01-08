/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.querydsl;

import com.querydsl.core.QueryModifiers;
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
 * @param <Q> Concrete query type
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
@SuppressWarnings("unused")
public interface JPQLNextQuery<T, Q extends JPQLNextQuery<T, Q>> extends JPQLQuery<T> {

    /**
     * Register a common table expression (CTE).
     * Analog to {@link com.querydsl.sql.ProjectableSQLQuery#with(Path, SubQueryExpression)}.
     *
     * @param alias The alias for the CTE
     * @param o The subquery expression
     * @param <X> CTE type
     * @return this query
     */
    <X> Q with(Path<X> alias, SubQueryExpression<?> o);

    /**
     * Register a recursive common table expression (CTE).
     * Analog to {@link com.querydsl.sql.ProjectableSQLQuery#withRecursive(Path, SubQueryExpression)}.
     *
     * @param alias The alias for the CTE
     * @param o The subquery expression
     * @param <X> CTE type
     * @return this query
     */
    <X> Q withRecursive(Path<X> alias, SubQueryExpression<?> o);

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
    WithBuilder<Q> with(EntityPath<?> alias, Path<?>... columns);

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
    WithBuilder<Q> withRecursive(EntityPath<?> alias, Path<?>... columns);

    /**
     * Select from a set of values using the {@code VALUES} clause.
     *
     * @param path Type of values
     * @param elements The elements
     * @param <X> The element type
     * @return this query
     */
    <X> Q fromValues(EntityPath<X> path, Collection<X> elements);

    /**
     * Select from a set of values using the {@code VALUES} clause.
     *
     * @param path Type of values
     * @param elements The elements
     * @param <X> The element type
     * @return this query
     */
    <X> Q fromIdentifiableValues(EntityPath<X> path, Collection<X> elements);

    /**
     * Select from a set of values using the {@code VALUES} clause.
     *
     * @param path Type of values
     * @param alias The alias from which the values can be referenced
     * @param elements The elements
     * @param <X> The element type
     * @return this query
     */
    <X> Q fromValues(Path<X> path, Path<X> alias, Collection<X> elements);

    /**
     * Select from a set of values using the {@code VALUES} clause.
     *
     * @param path Type of values
     * @param alias The alias from which the values can be referenced
     * @param elements The elements
     * @param <X> The element type
     * @return this query
     */
    <X> Q fromIdentifiableValues(Path<X> path, Path<X> alias, Collection<X> elements);

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
    <P> Q fullJoin(CollectionExpression<?, P> target);

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
    <P> Q fullJoin(CollectionExpression<?, P> target, Path<P> alias);

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
    <P> Q fullJoin(EntityPath<P> target);

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
    <P> Q fullJoin(EntityPath<P> target, Path<P> alias);

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
    <P> Q fullJoin(MapExpression<?, P> target);

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
    <P> Q fullJoin(MapExpression<?, P> target, Path<P> alias);

    /**
     * Add sources to this query
     *
     * @param subQueryExpression Subquery expression
     * @param alias The alias for the subquery from which it can be referenced in the outer query
     * @param <X> Expression type
     * @return the current object
     */
    <X> Q from(SubQueryExpression<X> subQueryExpression, Path<X> alias);

    /**
     * Adds a left join to the given subquery target.
     * Analog to {@link com.querydsl.sql.SQLCommonQuery#leftJoin(SubQueryExpression, Path)}.
     *
     * @param o subquery
     * @param alias alias
     * @param <X> Expression type
     * @return the current object
     */
    <X> Q leftJoin(SubQueryExpression<X> o, Path<X> alias);

    /**
     * Adds a right join to the given target.
     * Analog to {@link com.querydsl.sql.SQLCommonQuery#rightJoin(SubQueryExpression, Path)}.
     *
     * @param o subquery
     * @param alias alias
     * @param <X> Expression type
     * @return the current object
     */
    <X> Q rightJoin(SubQueryExpression<X> o, Path<X> alias);

    /**
     * Adds a full join to the given target.
     * Analog to {@link com.querydsl.sql.SQLCommonQuery#fullJoin(SubQueryExpression, Path)}.
     *
     * @param o subquery
     * @param alias alias
     * @param <X> Expression type
     * @return the current object
     */
    <X> Q fullJoin(SubQueryExpression<X> o, Path<X> alias);

    /**
     * Adds a inner join to the given target.
     * Analog to {@link com.querydsl.sql.SQLCommonQuery#innerJoin(SubQueryExpression, Path)}.
     *
     * @param o subquery
     * @param alias alias
     * @param <X> Expression type
     * @return the current object
     */
    <X> Q innerJoin(SubQueryExpression<X> o, Path<X> alias);

    /**
     * Creates an union expression for the given subqueries.
     * Analog to {@link com.querydsl.sql.ProjectableSQLQuery#union(List)}.
     *
     * @param <RT> set operation type
     * @param sq subqueries
     * @return the set operation result
     */
    <RT> SetExpression<RT> union(List<SubQueryExpression<RT>> sq);

    /**
     * Creates an union expression for the given subqueries.
     * Analog to {@link com.querydsl.sql.ProjectableSQLQuery#unionAll(List)}.
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
     * Analog to {@link com.querydsl.sql.ProjectableSQLQuery#union(List)}.
     *
     * @param <RT> set operation type
     * @param sq subqueries
     * @return the set operation result
     */
    @SuppressWarnings("unchecked")
    <RT> SetExpression<RT> union(SubQueryExpression<RT>... sq);

    /**
     * Creates an union expression for the given subqueries.
     * Analog to {@link com.querydsl.sql.ProjectableSQLQuery#unionAll(List)}.
     *
     * @param <RT> set operation type
     * @param sq subqueries
     * @return the set operation result
     */
    @SuppressWarnings("unchecked")
    <RT> SetExpression<RT> unionAll(SubQueryExpression<RT>... sq);

    /**
     * Creates an intersect expression for the given subqueries.
     *
     * @param <RT> set operation type
     * @param sq subqueries
     * @return the set operation result
     */
    @SuppressWarnings("unchecked")
    <RT> SetExpression<RT> intersect(SubQueryExpression<RT>... sq);

    /**
     * Creates an intersect expression for the given subqueries.
     *
     * @param <RT> set operation type
     * @param sq subqueries
     * @return the set operation result
     */
    @SuppressWarnings("unchecked")
    <RT> SetExpression<RT> intersectAll(SubQueryExpression<RT>... sq);

    /**
     * Creates an except expression for the given subqueries.
     *
     * @param <RT> set operation type
     * @param sq subqueries
     * @return the set operation result
     */
    @SuppressWarnings("unchecked")
    <RT> SetExpression<RT> except(SubQueryExpression<RT>... sq);

    /**
     * Creates an except expression for the given subqueries.
     *
     * @param <RT> set operation type
     * @param sq subqueries
     * @return the set operation result
     */
    @SuppressWarnings("unchecked")
    <RT> SetExpression<RT> exceptAll(SubQueryExpression<RT>... sq);

    /**
     * Mark the last join as a lateral join.
     * 
     * @return this query
     */
    Q lateral();

    // Covariant Overrides

    @Override
    Q from(EntityPath<?>... sources);

    @Override
    <P> Q from(CollectionExpression<?, P> target, Path<P> alias);

    @Override
    <P> Q innerJoin(EntityPath<P> target);

    @Override
    <P> Q innerJoin(EntityPath<P> target, Path<P> alias);

    @Override
    <P> Q innerJoin(CollectionExpression<?, P> target);

    @Override
    <P> Q innerJoin(CollectionExpression<?, P> target, Path<P> alias);

    @Override
    <P> Q innerJoin(MapExpression<?, P> target);

    @Override
    <P> Q innerJoin(MapExpression<?, P> target, Path<P> alias);

    @Override
    <P> Q join(EntityPath<P> target);

    @Override
    <P> Q join(EntityPath<P> target, Path<P> alias);

    @Override
    <P> Q join(CollectionExpression<?, P> target);

    @Override
    <P> Q join(CollectionExpression<?, P> target, Path<P> alias);

    @Override
    <P> Q join(MapExpression<?, P> target);

    @Override
    <P> Q join(MapExpression<?, P> target, Path<P> alias);

    @Override
    <P> Q leftJoin(EntityPath<P> target);

    @Override
    <P> Q leftJoin(EntityPath<P> target, Path<P> alias);

    @Override
    <P> Q leftJoin(CollectionExpression<?, P> target);

    @Override
    <P> Q leftJoin(CollectionExpression<?, P> target, Path<P> alias);

    @Override
    <P> Q leftJoin(MapExpression<?, P> target);

    @Override
    <P> Q leftJoin(MapExpression<?, P> target, Path<P> alias);

    @Override
    <P> Q rightJoin(EntityPath<P> target);

    @Override
    <P> Q rightJoin(EntityPath<P> target, Path<P> alias);

    @Override
    <P> Q rightJoin(CollectionExpression<?, P> target);

    @Override
    <P> Q rightJoin(CollectionExpression<?, P> target, Path<P> alias);

    @Override
    <P> Q rightJoin(MapExpression<?, P> target);

    @Override
    <P> Q rightJoin(MapExpression<?, P> target, Path<P> alias);

    @Override
    Q on(Predicate... condition);

    @Override
    Q fetchJoin();

    @Override
    Q fetchAll();

    @Override
    Q groupBy(Expression<?>... expressions);

    @Override
    Q having(Predicate... predicates);

    @Override
    Q limit(long l);

    @Override
    Q offset(long l);

    @Override
    Q restrict(QueryModifiers queryModifiers);

    @Override
    Q orderBy(OrderSpecifier<?>... orderSpecifiers);

    @Override
    <U> Q set(ParamExpression<U> paramExpression, U t);

    @Override
    Q distinct();

    @Override
    Q where(Predicate... predicates);

    /**
     * Add a named window to this query.
     *
     * @param namedWindow The window definition to add
     * @return this query
     */
    Q window(NamedWindow namedWindow);

}
