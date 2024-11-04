/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for modification queries.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface BaseModificationCriteriaBuilder<X extends BaseModificationCriteriaBuilder<X>> extends FromBuilder<X>, WhereBuilder<X> {

}
