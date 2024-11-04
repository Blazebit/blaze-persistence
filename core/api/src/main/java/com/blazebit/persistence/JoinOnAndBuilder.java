/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * The builder interface for a on predicate container that connects predicates with the AND operator.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface JoinOnAndBuilder<T> extends BaseJoinOnBuilder<JoinOnAndBuilder<T>> {

    /**
     * Finishes the AND predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     *
     * @return The parent predicate container builder
     */
    public T endAnd();

    /**
     * Starts a on or builder which connects it's predicates with the OR operator.
     * When the builder finishes, the predicate is added to this predicate container as conjunct.
     *
     * @return The on or builder
     */
    public JoinOnOrBuilder<JoinOnAndBuilder<T>> onOr();
}
