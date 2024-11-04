/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for CTE criteria queries. This is the entry point for building CTE queries.
 *
 * @param <X> The result type which is returned afte the CTE builder
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface SelectRecursiveCTECriteriaBuilder<X> extends SelectBaseCTECriteriaBuilder<SelectRecursiveCTECriteriaBuilder<X>> {

    /**
     * Finishes the CTE builder for the non-recursive part and starts the builder for the recursive part.
     * The union operator is used for connecting the non-recursive and recursive part, thus removing duplicates. 
     *
     * @return The parent query builder
     */
    public SelectCTECriteriaBuilder<X> union();
    
    /**
     * Finishes the CTE builder for the non-recursive part and starts the builder for the recursive part.
     * The union all operator is used for connecting the non-recursive and recursive part, thus not removing duplicates.
     *
     * @return The parent query builder
     */
    public SelectCTECriteriaBuilder<X> unionAll();
}
