/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * An interface for builders that support set operators.
 *
 * @param <T> The builder result type
 * @param <Y> The set sub-operation result type
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface MiddleOngoingSetOperationCTECriteriaBuilder<T, Y> extends OngoingSetOperationBuilder<OngoingSetOperationCTECriteriaBuilder<T, Y>, Y, StartOngoingSetOperationCTECriteriaBuilder<T, MiddleOngoingSetOperationCTECriteriaBuilder<T, Y>>> {

    /**
     * Finishes the current set operation builder and returns a final builder for ordering and limiting.
     *
     * @return The final builder for ordering and limiting
     */
    public OngoingFinalSetOperationCTECriteriaBuilder<Y> endSetWith();
}
