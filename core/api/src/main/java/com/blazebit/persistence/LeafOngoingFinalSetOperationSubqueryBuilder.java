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
 * @since 1.2.0
 */
public interface LeafOngoingFinalSetOperationSubqueryBuilder<X> extends BaseOngoingSetOperationBuilder<LeafOngoingSetOperationSubqueryBuilder<X>, FinalSetOperationSubqueryBuilder<X>, StartOngoingSetOperationSubqueryBuilder<X, LeafOngoingFinalSetOperationSubqueryBuilder<X>>> {
}
