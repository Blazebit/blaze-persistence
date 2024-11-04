/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.spi.type;

/**
 * A contract for defining a custom basic type to use with entity views for versions in optimistic concurrency control.
 *
 * @param <X> The type of the user type
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface VersionBasicUserType<X> extends BasicUserType<X> {

    /**
     * Returns the next version value based on the given current version value.
     *
     * @param current The current version value that may be <code>null</code>
     * @return The next value
     */
    public X nextValue(X current);
}
