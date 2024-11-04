/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for the escape part of a like predicate.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public interface EscapeBuilder<T> {

    /**
     * Sets the given character as the escape character.
     *
     * @param c The escape character
     * @return The parent builder
     */
    public T escape(char c);

    /**
     * Specifies that no escape character should be used.
     * 
     * @return The parent builder
     */
    public T noEscape();
}
