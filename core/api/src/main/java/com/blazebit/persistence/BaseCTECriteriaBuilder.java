/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for cte criteria queries. This is the entry point for building cte queries.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface BaseCTECriteriaBuilder<X extends BaseCTECriteriaBuilder<X>> extends CommonQueryBuilder<X>, FromBuilder<X>, KeysetQueryBuilder<X>, WhereBuilder<X>, OrderByBuilder<X>, GroupByBuilder<X>, DistinctBuilder<X>, LimitBuilder<X>, WindowContainerBuilder<X> {
// TODO: maybe also add CTEBuilder?
}
