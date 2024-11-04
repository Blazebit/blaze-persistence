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
 * @since 1.1.0
 */
public interface OngoingSetOperationCriteriaBuilder<T, Y> extends MiddleOngoingSetOperationCriteriaBuilder<T, Y>, BaseCriteriaBuilder<T, OngoingSetOperationCriteriaBuilder<T, Y>> {
}
