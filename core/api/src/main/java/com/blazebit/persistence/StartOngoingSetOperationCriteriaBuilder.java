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
public interface StartOngoingSetOperationCriteriaBuilder<X, Y> extends MiddleOngoingSetOperationCriteriaBuilder<X, Y>, StartOngoingSetOperationBuilder<OngoingSetOperationCriteriaBuilder<X, Y>, Y, StartOngoingSetOperationCriteriaBuilder<X, MiddleOngoingSetOperationCriteriaBuilder<X, Y>>>, BaseCriteriaBuilder<X, StartOngoingSetOperationCriteriaBuilder<X, Y>>, CTEBuilder<StartOngoingSetOperationCriteriaBuilder<X, Y>> {

}
