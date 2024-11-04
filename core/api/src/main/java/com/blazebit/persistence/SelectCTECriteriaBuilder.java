/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for cte criteria queries. This is the entry point for building cte queries.
 *
 * @param <X> The result type which is returned after the CTE builder
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface SelectCTECriteriaBuilder<X> extends SelectBaseCTECriteriaBuilder<SelectCTECriteriaBuilder<X>> {

    /**
     * Finishes the CTE builder.
     *
     * @return The parent query builder
     */
    public X end();
}
