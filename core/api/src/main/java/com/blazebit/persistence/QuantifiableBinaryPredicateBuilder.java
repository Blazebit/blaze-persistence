/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * The interface for quantifiable binary predicate builders.
 * The left hand side and the operator are already known to the builder and the methods of this builder either terminate the building
 * process or start a {@link SubqueryInitiator}.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface QuantifiableBinaryPredicateBuilder<T> extends BinaryPredicateBuilder<T>, SubqueryInitiator<T> {

    /**
     * Starts a {@link SubqueryInitiator} for the right hand side of a predicate that uses the ALL quantor.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<T> all();

    /**
     * Starts a {@link SubqueryInitiator} for the right hand side of a predicate that uses the ANY quantor.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<T> any();

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the right hand side of a predicate that uses the ALL quantor.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<T> all(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the right hand side of a predicate that uses the ANY quantor.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<T> any(FullQueryBuilder<?, ?> criteriaBuilder);
}
