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
public interface StartOngoingSetOperationSubqueryBuilder<X, Y> extends MiddleOngoingSetOperationSubqueryBuilder<X, Y>, StartOngoingSetOperationBuilder<OngoingSetOperationSubqueryBuilder<X, Y>, Y, StartOngoingSetOperationSubqueryBuilder<X, MiddleOngoingSetOperationSubqueryBuilder<X, Y>>>, BaseSubqueryBuilder<StartOngoingSetOperationSubqueryBuilder<X, Y>> {

}
