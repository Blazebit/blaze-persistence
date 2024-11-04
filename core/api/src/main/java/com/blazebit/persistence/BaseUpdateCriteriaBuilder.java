/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for update queries.
 *
 * @param <T> The entity type for which this update query is
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface BaseUpdateCriteriaBuilder<T, X extends BaseUpdateCriteriaBuilder<T, X>> extends BaseModificationCriteriaBuilder<X> {

    /**
     * Binds the given value as parameter to the attribute.
     *
     * @param attribute The attribute for which the value should be bound
     * @param value The value that should be bound
     * @return The query builder for chaining calls
     */
    public X set(String attribute, Object value);

    /**
     * Binds <code>NULL</code> to the attribute.
     *
     * @param attribute The attribute for which <code>NULL</code> should be bound
     * @return The query builder for chaining calls
     * @since 1.5.0
     */
    public X setNull(String attribute);

    /**
     * Binds the given expression to the attribute.
     *
     * @param attribute The attribute for which the expression should be bound
     * @param expression The expression that should be bound
     * @return The query builder for chaining calls
     */
    public X setExpression(String attribute, String expression);

    /**
     * Starts a subquery builder for creating an expression that should be bound to the attribute.
     *
     * @param attribute The attribute for which the subquery expression should be bound
     * @return The query builder for chaining calls
     */
    public SubqueryInitiator<X> set(String attribute);

    /**
     * Starts a subquery builder for creating an expression that should be bound to the attribute based on the given criteria builder.
     *
     * @param attribute The attribute for which the subquery expression should be bound
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<X> set(String attribute, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts a {@link MultipleSubqueryInitiator} with the given expression that should bound to the attribute.
     *
     * <p>
     * All occurrences of subsequently defined <code>subqueryAlias</code>es in <code>expression</code> will be replaced by the respective subquery.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     * </p>
     *
     * @param attribute The attribute for which the expression should be bound
     * @param expression The expression for the lower bound of the between predicate.
     * @return The subquery initiator for building multiple subqueries for their respective subqueryAliases
     */
    public MultipleSubqueryInitiator<X> setSubqueries(String attribute, String expression);
    
}
