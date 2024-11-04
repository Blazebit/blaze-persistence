/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * An interface for building a window frame between clause for analytics functions.
 *
 * @param <T> The builder return type
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface WindowFrameBetweenBuilder<T> {

    /**
     * Uses UNBOUNDED FOLLOWING as upper bound and continues to the frame exclusion builder.
     *
     * @return The frame exclusion builder
     */
    WindowFrameExclusionBuilder<T> andUnboundedFollowing();

    /**
     * Uses X PRECEDING as upper bound and continues to the frame exclusion builder.
     *
     * @param expression The expression for the frame bound
     * @return The frame exclusion builder
     */
    WindowFrameExclusionBuilder<T> andPreceding(String expression);

    /**
     * Uses X FOLLOWING as upper bound and continues to the frame exclusion builder.
     *
     * @param expression The expression for the frame bound
     * @return The frame exclusion builder
     */
    WindowFrameExclusionBuilder<T> andFollowing(String expression);

    /**
     * Uses CURRENT ROW as upper bound and continues to the frame exclusion builder.
     *
     * @return The frame exclusion builder
     */
    WindowFrameExclusionBuilder<T> andCurrentRow();
}
