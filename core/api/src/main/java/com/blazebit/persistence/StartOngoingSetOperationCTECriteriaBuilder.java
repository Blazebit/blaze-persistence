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
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface StartOngoingSetOperationCTECriteriaBuilder<X, Y> extends MiddleOngoingSetOperationCTECriteriaBuilder<X, Y>, StartOngoingSetOperationBuilder<OngoingSetOperationCTECriteriaBuilder<X, Y>, Y, StartOngoingSetOperationCTECriteriaBuilder<X, MiddleOngoingSetOperationCTECriteriaBuilder<X, Y>>>, SelectBaseCTECriteriaBuilder<StartOngoingSetOperationCTECriteriaBuilder<X, Y>> {
}
