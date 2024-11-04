/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * The builder interface for a having predicate container that connects predicates with the AND operator.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface HavingAndBuilder<T> extends BaseHavingBuilder<HavingAndBuilder<T>> {

    /**
     * Finishes the AND predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     *
     * @return The parent predicate container builder
     */
    public T endAnd();

    /**
     * Starts a having or builder which connects it's predicates with the OR operator.
     * When the builder finishes, the predicate is added to this predicate container as conjunct.
     *
     * @return The having or builder
     */
    public HavingOrBuilder<HavingAndBuilder<T>> havingOr();
}
