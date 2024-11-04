/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for update queries.
 *
 * @param <T> The entity type for which this update query is
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface UpdateCriteriaBuilder<T> extends ModificationCriteriaBuilder<UpdateCriteriaBuilder<T>>, BaseUpdateCriteriaBuilder<T, UpdateCriteriaBuilder<T>> {

}
