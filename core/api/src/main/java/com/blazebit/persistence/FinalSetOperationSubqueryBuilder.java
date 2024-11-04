/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * An interface for builders that support set operators.
 *
 * @param <T> The result type which is returned after the subquery builder
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface FinalSetOperationSubqueryBuilder<T> extends BaseFinalSetOperationBuilder<T, FinalSetOperationSubqueryBuilder<T>> {

    /**
     * Finishes the CTE builder.
     *
     * @return The parent query builder
     */
    public T end();
}
