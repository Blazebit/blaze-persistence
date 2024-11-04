/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * An interface for builders that support group by.
 * This is related to the fact, that a query builder supports group by clauses.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface GroupByBuilder<X extends GroupByBuilder<X>> extends HavingBuilder<X> {

    /**
     * Adds a group by clause with the given expressions to the query.
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

    /**
     * Adds a group by clause with a rollup of the given expressions to the query.
     *
     * @param expressions The expressions for rollup for the group by clauses
     * @return The query builder for chaining calls
     */
    public X groupByRollup(String... expressions);

    /**
     * Adds a group by clause with a cube of the given expressions to the query.
     *
     * @param expressions The expressions for cube for the group by clauses
     * @return The query builder for chaining calls
     */
    public X groupByCube(String... expressions);

    /**
     * Adds a group by clause with a rollup of the given expressions to the query.
     *
     * @param expressions The expressions for rollup for the group by clauses
     * @return The query builder for chaining calls
     */
    public X groupByRollup(String[]... expressions);

    /**
     * Adds a group by clause with a cube of the given expressions to the query.
     *
     * @param expressions The expressions for cube for the group by clauses
     * @return The query builder for chaining calls
     */
    public X groupByCube(String[]... expressions);

    /**
     * Adds a group by clause with a grouping set of the given expressions to the query.
     *
     * @param expressions The expressions for grouping set for the group by clauses
     * @return The query builder for chaining calls
     */
    public X groupByGroupingSets(String[]... expressions);

    // NOTE: JPA 4.6.16 says that subqueries are only allowed in WHERE and HAVING
    // TODO: group by subqueries?
}
