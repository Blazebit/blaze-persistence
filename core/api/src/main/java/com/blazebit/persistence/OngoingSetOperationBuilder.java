/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A base interface for builders that support set operators.
 *
 * @param <X> The concrete builder type
 * @param <Y> The set sub-operation result type
 * @param <Z> The set nesting start type 
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface OngoingSetOperationBuilder<X, Y, Z extends StartOngoingSetOperationBuilder<?, ?, ?>> extends BaseOngoingSetOperationBuilder<X, Y, Z> {

    /**
     * Finishes the current set operation builder and returns a final builder for ordering and limiting.
     *
     * @return The final builder for ordering and limiting
     */
    public BaseOngoingFinalSetOperationBuilder<Y, ?> endSetWith();
}
