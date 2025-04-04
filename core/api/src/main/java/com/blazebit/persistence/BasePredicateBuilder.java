/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A base interface for builders that support filtering.
 * This is related to the fact, that a query builder supports predicate clauses.
 *
 * @param <T> The concrete builder type
 * @author Christian Beikov
 * @since 1.6.8
 */
public interface BasePredicateBuilder<T extends BasePredicateBuilder<T>> {

    /**
     * Starts a {@link SubqueryInitiator} for the left hand side of a predicate.
     * When the subquery builder and the restriction builder for the right hand side are finished, the predicate is added to the
     * parent predicate container represented by the type <code>T</code>.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<RestrictionBuilder<T>> subquery();

    /**
     * Starts a {@link SubqueryInitiator} for the left hand side of a predicate.
     * 
     * <p>
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery. When the subquery
     * builder and the restriction builder for the right hand side are finished, the predicate is added to the parent predicate
     * container represented by the type <code>T</code>.
     * </p>
     * 
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be used as left hand side of a predicate.
     *            This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<RestrictionBuilder<T>> subquery(String subqueryAlias, String expression);

    /**
     * Starts a {@link MultipleSubqueryInitiator} for the left hand side of a predicate.
     * 
     * <p>
     * All occurrences of subsequently defined <code>subqueryAlias</code>es in <code>expression</code> will be replaced by the respective subquery.
     * When the builder finishes, the resulting expression is used for the left hand side of the predicate.
     * </p>
     * 
     * @param expression The expression which will be used as left hand side of a predicate
     * @return The subquery initiator for building multiple subqueries for their respective subqueryAliases
     */
    public MultipleSubqueryInitiator<RestrictionBuilder<T>> subqueries(String expression);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the left hand side of a predicate.
     * When the subquery builder and the restriction builder for the right hand side are finished, the predicate is added to the
     * parent predicate container represented by the type <code>T</code>.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     */
    public SubqueryBuilder<RestrictionBuilder<T>> subquery(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the left hand side of a predicate. All occurrences of <code>subqueryAlias</code> in
     * <code>expression</code> will be replaced by the subquery.
     * When the subquery builder and the restriction builder for the right hand side are finished, the predicate is added to the
     * parent predicate container represented by the type <code>T</code>.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be used as left hand side of a predicate
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     */
    public SubqueryBuilder<RestrictionBuilder<T>> subquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Adds the given expression as expression for the where clause.
     *
     * @param expression The where expression
     * @return The builder
     */
    public T withExpression(String expression);

    /**
     * Starts a {@link MultipleSubqueryInitiator} for expression of the where clause.
     *
     * <p>
     * All occurrences of subsequently defined <code>subqueryAlias</code>es in <code>expression</code> will be replaced by the respective subquery.
     * When the builder finishes, the resulting expression is added as expression to the parent predicate container represented by the type <code>T</code>.
     * </p>
     *
     * @param expression The where expression
     * @return The subquery initiator for building multiple subqueries for their respective subqueryAliases
     */
    public MultipleSubqueryInitiator<T> withExpressionSubqueries(String expression);

    /**
     * Starts a {@link RestrictionBuilder} for a where predicate with the given expression as left hand expression.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @param expression The left hand expression for a where predicate
     * @return The restriction builder for the given expression
     */
    public RestrictionBuilder<T> expression(String expression);

    /**
     * Starts a {@link CaseWhenBuilder} for a where predicate.
     * When the {@link CaseWhenBuilder} and the restriction builder for the right hand side are finished,
     * the predicate is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @return A {@link CaseWhenBuilder}
     */
    public CaseWhenStarterBuilder<RestrictionBuilder<T>> selectCase();

    /**
     * Starts a {@link SimpleCaseWhenBuilder} for a where predicate.
     * When the {@link CaseWhenBuilder} and the restriction builder for the right hand side are finished,
     * the predicate is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @param expression Case operand expression
     * @return A {@link CaseWhenBuilder}
     */
    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<T>> selectCase(String expression);

    /**
     * Starts an exists predicate for the where clause with a subquery on the right hand side.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<T> exists();

    /**
     * Starts an not exists predicate for the where clause with a subquery on the right hand side.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<T> notExists();

    /**
     * Starts an exists predicate for the where clause with a subquery on the right hand side based on the given criteria builder.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     */
    public SubqueryBuilder<T> exists(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts an exists predicate for the where clause with a subquery on the right hand side based on the given criteria builder.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     */
    public SubqueryBuilder<T> notExists(FullQueryBuilder<?, ?> criteriaBuilder);
}
