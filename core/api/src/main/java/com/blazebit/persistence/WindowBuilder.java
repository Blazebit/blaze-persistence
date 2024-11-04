/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * An interface for building a window for analytics functions.
 *
 * @param <T> The builder return type
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface WindowBuilder<T> extends OrderByBuilder<WindowBuilder<T>> {

    /**
     * Starts a filter builder.
     *
     * @param <X> The filter builder type
     * @return The filter builder
     */
    public <X extends WhereBuilder<X> & WindowBuilder<X>> X filter();

    /**
     * Adds a partition by clause with the given expressions to the window.
     *
     * @param partitionExpressions The expressions for the partition by clause
     * @return The window builder for chaining calls
     */
    public WindowBuilder<T> partitionBy(String... partitionExpressions);

    /**
     * Adds a partition by clause with the given expression to the window.
     *
     * @param partitionExpression The expression for the partition by clause
     * @return The window builder for chaining calls
     */
    public WindowBuilder<T> partitionBy(String partitionExpression);

    /**
     * Starts a frame builder with the ROWS frame mode.
     *
     * @return The frame builder
     */
    public WindowFrameBuilder<T> rows();

    /**
     * Starts a frame builder with the RANGE frame mode.
     *
     * @return The frame builder
     */
    public WindowFrameBuilder<T> range();

    /**
     * Starts a frame builder with the GROUPS frame mode.
     *
     * @return The frame builder
     */
    public WindowFrameBuilder<T> groups();

    /**
     * Finishes the window builder.
     *
     * @return The parent query builder
     */
    public T end();
}
