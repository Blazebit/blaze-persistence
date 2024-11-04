/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for update queries.
 *
 * @param <T> The entity type for which this update query is
 * @param <X> The parent query build type 
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface ReturningUpdateCriteriaBuilder<T, X> extends ReturningModificationCriteriaBuilder<ReturningUpdateCriteriaBuilder<T, X>, X>, BaseUpdateCriteriaBuilder<T, ReturningUpdateCriteriaBuilder<T, X>> {
    
}
