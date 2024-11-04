/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for modification queries.
 *
 * @param <X> The concrete builder type
 * @param <Y> The parent query build type 
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface ReturningModificationCriteriaBuilder<X extends ReturningModificationCriteriaBuilder<X, Y>, Y> extends CommonQueryBuilder<X>, BaseModificationCriteriaBuilder<X>, ReturningBuilder<X> {

    /**
     * Finishes the returning builder and returns the parent builder.
     *
     * @return The parent builder
     */
    public Y end();
    
}
