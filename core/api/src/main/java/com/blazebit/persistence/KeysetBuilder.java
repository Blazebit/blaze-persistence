/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for keysets to make use of keyset pagination.
 * This is used for building a keyset declaratively.
 *
 * @param <T> The builder result type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface KeysetBuilder<T> {

    /**
     * Uses the given value as reference value for keyset pagination for the given expression.
     * Normally the expression is one of the order by expressions used in the query.
     *
     * @param expression The order by expression for which a value should be provided
     * @param value The reference value from which the keyset pagination can start from
     * @return This keyset builder
     */
    public KeysetBuilder<T> with(String expression, Object value);

    /**
     * Finishes the keyset builder.
     *
     * @return The query builder
     */
    public T end();
}
