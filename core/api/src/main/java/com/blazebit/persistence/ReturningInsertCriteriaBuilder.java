/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for insert queries.
 *
 * @param <T> The entity type for which this update query is
 * @param <X> The parent query build type 
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface ReturningInsertCriteriaBuilder<T, X> extends ReturningModificationCriteriaBuilder<ReturningInsertCriteriaBuilder<T, X>, X>, BaseInsertCriteriaBuilder<T, ReturningInsertCriteriaBuilder<T, X>> {
    
}
