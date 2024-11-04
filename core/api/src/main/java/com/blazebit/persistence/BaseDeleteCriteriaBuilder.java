/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for delete queries.
 *
 * @param <T> The entity type for which this delete query is
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface BaseDeleteCriteriaBuilder<T, X extends BaseDeleteCriteriaBuilder<T, X>> extends BaseModificationCriteriaBuilder<X> {
    
}
