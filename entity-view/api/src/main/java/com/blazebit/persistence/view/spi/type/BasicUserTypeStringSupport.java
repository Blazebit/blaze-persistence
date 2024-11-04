/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.spi.type;

/**
 * A contract for defining string support for a custom basic type to use with entity views.
 *
 * @param <X> The type of the user type
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface BasicUserTypeStringSupport<X> {

    /**
     * Creates an instance of the type from the given string representation.
     *
     * @param sequence A string representation of the object
     * @return The object
     * @since 1.5.0
     */
    public X fromString(CharSequence sequence);

    /**
     * Wraps the given JPQL.Next expression such that it is converted to the internal string representation that can be read by {@link #fromString(CharSequence)}.
     *
     * @param expression The JPQL.Next expression string
     * @return The object
     * @since 1.5.0
     */
    public String toStringExpression(String expression);
}
