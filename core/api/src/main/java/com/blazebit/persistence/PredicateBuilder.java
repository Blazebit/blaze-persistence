/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * An interface for builders that support filtering. This is related to the
 * fact, that a query builder supports where clauses.
 *
 * @author Christian Beikov
 * @since 1.6.8
 */
public interface PredicateBuilder extends BasePredicateBuilder<PredicateBuilder> {

    /**
     * Starts a {@link PredicateOrBuilder} which is a predicate consisting only of
     * disjunctiv connected predicates. When the builder finishes, the predicate
     * is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @return The or predicate builder for the where clause
     */
    public PredicateOrBuilder<PredicateBuilder> or();

    /**
     * Sets the given expression as expression for the where clause.
     *  
     * @param expression The where expression
     * @return The builder
     */
    public PredicateBuilder setExpression(String expression);
    
    /**
     * Starts a {@link MultipleSubqueryInitiator} for expression of the where clause.
     * 
     * <p>
     * All occurrences of subsequently defined <code>subqueryAlias</code>es in <code>expression</code> will be replaced by the respective subquery.
     * When the builder finishes, the resulting expression is used as expression for the where clause.
     * </p>
     *  
     * @param expression The where expression
     * @return The subquery initiator for building multiple subqueries for their respective subqueryAliases
     */
    public MultipleSubqueryInitiator<PredicateBuilder> setExpressionSubqueries(String expression);
}
