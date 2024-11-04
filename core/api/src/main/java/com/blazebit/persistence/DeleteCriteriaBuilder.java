/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for delete queries.
 *
 * @param <T> The entity type for which this delete query is
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface DeleteCriteriaBuilder<T> extends ModificationCriteriaBuilder<DeleteCriteriaBuilder<T>>, BaseDeleteCriteriaBuilder<T, DeleteCriteriaBuilder<T>> {
    
}
