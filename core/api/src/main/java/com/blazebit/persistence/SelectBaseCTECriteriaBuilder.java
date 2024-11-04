/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for cte criteria queries that select. This is the entry point for building cte queries.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface SelectBaseCTECriteriaBuilder<X extends SelectBaseCTECriteriaBuilder<X>> extends BaseCTECriteriaBuilder<X> {

    /**
     * Starts a select builder for building an expression to bind to the CTE attribute.
     *
     * @param cteAttribute The CTE attribute to which the resulting expression should be bound
     * @return A select builder for building an expression
     */
    public SelectBuilder<X> bind(String cteAttribute);
}
