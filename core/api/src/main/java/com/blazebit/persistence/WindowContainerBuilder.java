/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A base interface for builders that support adding named windows for analytics functions.
 *
 * @param <T> The concrete builder type
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface WindowContainerBuilder<T extends WindowContainerBuilder<T>> {

    /**
     * Starts a {@link WindowBuilder} that can be referenced by name in analytics functions.
     *
     * @param name The name of the window
     * @return The window builder for building the window for analytics functions
     */
    public WindowBuilder<T> window(String name);
}
