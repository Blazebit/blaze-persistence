/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * The builder interface for a where predicate container that connects predicates with the OR operator.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.6.8
 */
public interface PredicateOrBuilder<T> extends BasePredicateBuilder<PredicateOrBuilder<T>> {

    /**
     * Finishes the OR predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     *
     * @return The parent predicate container builder
     */
    public T endOr();

    /**
     * Starts a where and builder which connects it's predicates with the AND operator.
     * When the builder finishes, the predicate is added to this predicate container as disjunct.
     *
     * @return The where and builder
     */
    public PredicateAndBuilder<PredicateOrBuilder<T>> and();
}
