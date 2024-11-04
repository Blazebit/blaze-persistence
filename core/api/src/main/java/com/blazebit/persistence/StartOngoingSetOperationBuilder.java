/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * An interface for builders that support set operators.
 *
 * @param <X> The concrete builder type
 * @param <Y> The set sub-operation result type
 * @param <Z> The set nesting start type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface StartOngoingSetOperationBuilder<X, Y, Z extends StartOngoingSetOperationBuilder<?, ?, ?>> extends BaseOngoingSetOperationBuilder<X, Y, Z> {
    
    /**
     * Starts a nested set operation builder.
     * Doing this is like starting a nested query that will be connected via a set operation.
     *
     * @return The set operation builder
     * @since 1.2.0
     */
    public Z startSet();
}
