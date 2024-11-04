/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * The builder interface for a where predicate container that connects predicates with the AND operator.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.6.8
 */
public interface PredicateAndBuilder<T> extends BasePredicateBuilder<PredicateAndBuilder<T>> {

    /**
     * Finishes the AND predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     *
     * @return The parent predicate container builder
     */
    public T endAnd();

    /**
     * Starts a where or builder which connects it's predicates with the OR operator.
     * When the builder finishes, the predicate is added to this predicate container as conjunct.
     *
     * @return The where or builder
     */
    public PredicateOrBuilder<PredicateAndBuilder<T>> or();
}
