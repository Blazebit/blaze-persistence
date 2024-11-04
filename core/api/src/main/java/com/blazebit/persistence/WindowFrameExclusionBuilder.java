/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * An interface for building a window frame exclusion clause for analytics functions.
 *
 * @param <T> The builder return type
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface WindowFrameExclusionBuilder<T> {

    /**
     * Finishes the window builder.
     *
     * @return The parent query builder
     */
    T excludeNoOthers();

    /**
     * Finishes the window builder.
     *
     * @return The parent query builder
     */
    T excludeCurrentRow();

    /**
     * Finishes the window builder.
     *
     * @return The parent query builder
     */
    T excludeGroup();

    /**
     * Finishes the window builder.
     *
     * @return The parent query builder
     */
    T excludeTies();

    /**
     * Synonym for {@link #excludeNoOthers()}.
     *
     * @return The parent query builder
     */
    public T end();
}
