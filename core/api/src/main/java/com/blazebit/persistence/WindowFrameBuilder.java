/*
 * Copyright 2014 - 2022 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence;

/**
 * An interface for building a window frame clause for analytics functions.
 *
 * @param <T> The builder return type
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface WindowFrameBuilder<T> {

    /**
     * Uses UNBOUNDED PRECEDING as lower bound for the frame and starts a frame between builder for the upper bound.
     *
     * @return The frame between builder
     */
    WindowFrameBetweenBuilder<T> betweenUnboundedPreceding();

    /**
     * Uses expression PRECEDING as lower bound for the frame and starts a frame between builder for the upper bound.
     *
     * @param expression The expression for the frame bound
     * @return The frame between builder
     */
    WindowFrameBetweenBuilder<T> betweenPreceding(String expression);

    /**
     * Uses expression FOLLOWING as lower bound for the frame and starts a frame between builder for the upper bound.
     *
     * @param expression The expression for the frame bound
     * @return The frame between builder
     */
    WindowFrameBetweenBuilder<T> betweenFollowing(String expression);

    /**
     * Uses CURRENT ROW as lower bound for the frame and starts a frame between builder for the upper bound.
     *
     * @return The frame between builder
     */
    WindowFrameBetweenBuilder<T> betweenCurrentRow();

    /**
     * Uses UNBOUNDED PRECEDING as lower bound and continues to the frame exclusion builder.
     *
     * @return The frame exclusion builder
     */
    WindowFrameExclusionBuilder<T> unboundedPreceding();

    /**
     * Uses X PRECEDING as lower bound and continues to the frame exclusion builder.
     *
     * @param expression The expression for the frame bound
     * @return The frame exclusion builder
     */
    WindowFrameExclusionBuilder<T> preceding(String expression);

    /**
     * Uses CURRENT ROW as lower bound and continues to the frame exclusion builder.
     *
     * @return The frame exclusion builder
     */
    WindowFrameExclusionBuilder<T> currentRow();
}
