/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for criteria queries. This is the entry point for building queries.
 *
 * @param <T> The query result type
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface BaseCriteriaBuilder<T, X extends BaseCriteriaBuilder<T, X>> extends BaseQueryBuilder<T, X>, GroupByBuilder<X>, DistinctBuilder<X>, LimitBuilder<X> {

}
