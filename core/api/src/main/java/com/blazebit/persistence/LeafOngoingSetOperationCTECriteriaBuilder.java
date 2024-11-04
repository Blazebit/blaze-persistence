/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * An interface for builders that support set operators.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface LeafOngoingSetOperationCTECriteriaBuilder<X> extends BaseOngoingSetOperationBuilder<LeafOngoingSetOperationCTECriteriaBuilder<X>, FinalSetOperationCTECriteriaBuilder<X>, StartOngoingSetOperationCTECriteriaBuilder<X, LeafOngoingFinalSetOperationCTECriteriaBuilder<X>>>, SelectBaseCTECriteriaBuilder<LeafOngoingSetOperationCTECriteriaBuilder<X>> {
}
