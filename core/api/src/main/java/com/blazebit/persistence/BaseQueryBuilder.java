/*
 * Copyright 2014 Blazebit.
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

import javax.persistence.Tuple;

/**
 * A base interface for builders that support basic query functionality.
 * This interface is shared between normal query builders and subquery builders.
 *
 * @param <T> The query result type
 * @param <X> The concrete builder type
 * @author Christian Beikov
 */
public interface BaseQueryBuilder<T, X extends BaseQueryBuilder<T, X>> extends Aggregateable<X>, Filterable<X> {

    /**
     * Returns the query string for the built query.
     *
     * @return The query string
     */
    public String getQueryString();

    /*
     * Join methods
     */
    /**
     * Adds a join to the query, possibly specializing implicit joins, and giving the joined element an alias.
     *
     * @param path  The path to join
     * @param alias The alias for the joined element
     * @param type  The join type
     * @return The query builder for chaining calls
     */
    public X join(String path, String alias, JoinType type);

    /**
     * Like {@link BaseQueryBuilder#join(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with {@link JoinType#INNER}.
     *
     * @param path  The path to join
     * @param alias The alias for the joined element
     * @return The query builder for chaining calls
     */
    public X innerJoin(String path, String alias);

    /**
     * Like {@link BaseQueryBuilder#join(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with {@link JoinType#LEFT}.
     *
     * @param path  The path to join
     * @param alias The alias for the joined element
     * @return The query builder for chaining calls
     */
    public X leftJoin(String path, String alias);

    /**
     * Like {@link BaseQueryBuilder#join(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with {@link JoinType#OUTER}.
     *
     * @param path  The path to join
     * @param alias The alias for the joined element
     * @return The query builder for chaining calls
     */
    public X outerJoin(String path, String alias);

    /**
     * Like {@link BaseQueryBuilder#join(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with {@link JoinType#RIGHT}.
     *
     * @param path  The path to join
     * @param alias The alias for the joined element
     * @return The query builder for chaining calls
     */
    public X rightJoin(String path, String alias);

    /*
     * Select methods
     */
    /**
     * Marks the query to do a distinct select.
     *
     * @return The query builder for chaining calls
     */
    public X distinct();

    /**
     * TODO: javadoc
     *
     * @return
     */
    public CaseWhenBuilder<? extends X> selectCase();

    /* CASE caseOperand (WHEN scalarExpression THEN scalarExpression)+ ELSE scalarExpression END */
    /**
     * TODO: javadoc
     *
     * @return
     */
    public SimpleCaseWhenBuilder<? extends X> selectCase(String expression);

    /**
     * Adds a select clause with the given expression to the query.
     *
     * @param expression The expression for the select clause
     * @return The query builder for chaining calls
     */
    public BaseQueryBuilder<Tuple, ?> select(String expression);

    /**
     * Adds a select clause with the given expression and alias to the query.
     *
     * @param expression The expression for the select clause
     * @param alias      The alias for the expression
     * @return The query builder for chaining calls
     */
    public BaseQueryBuilder<Tuple, ?> select(String expression, String alias);

    /*
     * Order by methods
     */
    /**
     * Adds an order by clause with the given expression to the query.
     *
     * @param expression The expression for the order by clause
     * @param ascending  Wether the order should be ascending or descending.
     * @param nullFirst  Wether null elements should be ordered first or not.
     * @return The query builder for chaining calls
     */
    public X orderBy(String expression, boolean ascending, boolean nullFirst);

    /**
     * Like {@link BaseQueryBuilder#orderByAsc(java.lang.String, boolean) } but with <code>nullFirst</code> set to false.
     *
     * @param expression The expression for the order by clause
     * @return The query builder for chaining calls
     */
    public X orderByAsc(String expression);

    /**
     * Like {@link BaseQueryBuilder#orderBy(java.lang.String, boolean, boolean) } but with <code>ascending</code> set to true.
     *
     * @param expression The expression for the order by clause
     * @param nullFirst  Wether null elements should be ordered first or not.
     * @return The query builder for chaining calls
     */
    public X orderByAsc(String expression, boolean nullFirst);

    /**
     * Like {@link BaseQueryBuilder#orderByDesc(java.lang.String, boolean) } but with <code>nullFirst</code> set to false.
     *
     * @param expression The expression for the order by clause
     * @return The query builder for chaining calls
     */
    public X orderByDesc(String expression);

    /**
     * Like {@link BaseQueryBuilder#orderBy(java.lang.String, boolean, boolean) } but with <code>ascending</code> set to false.
     *
     * @param expression The expression for the order by clause
     * @param nullFirst  Wether null elements should be ordered first or not.
     * @return The query builder for chaining calls
     */
    public X orderByDesc(String expression, boolean nullFirst);

    /*
     * Where methods
     */
    /**
     * Starts a {@link WhereOrBuilder} which is a predicate consisting only of disjunctiv connected predicates.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type {@linkplain X}.
     *
     * @return The or predicate builder for the where clause
     */
    public WhereOrBuilder<? extends X> whereOr();

    /*
     * Group by methods
     */
    /**
     * Adds a multiple group by clause with the given expressions to the query.
     *
     * @param expressions The expressions for the group by clauses
     * @return The query builder for chaining calls
     */
    public X groupBy(String... expressions);

    /**
     * Adds a group by clause with the given expression to the query.
     *
     * @param expression The expression for the group by clause
     * @return The query builder for chaining calls
     */
    public X groupBy(String expression);

    /*
     * Having methods
     */
    /**
     * Starts a {@link HavingOrBuilder} which is a predicate consisting only of disjunctiv connected predicates.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type {@linkplain X}.
     *
     * @return The or predicate builder for the having clause
     */
    public HavingOrBuilder<? extends X> havingOr();
}
