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
public interface MiddleOngoingSetOperationCriteriaBuilder<T, Y> extends OngoingSetOperationBuilder<OngoingSetOperationCriteriaBuilder<T, Y>, Y, StartOngoingSetOperationCriteriaBuilder<T, MiddleOngoingSetOperationCriteriaBuilder<T, Y>>> {
    
    @Override
    public OngoingFinalSetOperationCriteriaBuilder<Y> endSetWith();
}
