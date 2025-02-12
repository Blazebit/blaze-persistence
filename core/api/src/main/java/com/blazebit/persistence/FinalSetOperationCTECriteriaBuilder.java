/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * An interface for builders that support set operators.
 *
 * @param <T> The builder result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface FinalSetOperationCTECriteriaBuilder<T> extends BaseFinalSetOperationBuilder<T, FinalSetOperationCTECriteriaBuilder<T>> {

    /**
     * Finishes the CTE builder.
     *
     * @return The parent query builder
     */
    public T end();
}
