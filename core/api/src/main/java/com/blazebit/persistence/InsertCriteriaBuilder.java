/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for insert queries.
 *
 * @param <T> The entity type for which this update query is
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface InsertCriteriaBuilder<T> extends ModificationCriteriaBuilder<InsertCriteriaBuilder<T>>, BaseInsertCriteriaBuilder<T, InsertCriteriaBuilder<T>> {
    
}
