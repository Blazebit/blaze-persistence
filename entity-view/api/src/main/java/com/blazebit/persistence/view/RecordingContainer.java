/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

/**
 * A recording collection.
 *
 * @param <C> The delegate collection type
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface RecordingContainer<C> {
    /**
     * Returns the delegate collection.
     *
     * @return The delegate collection
     */
    C getDelegate();
}