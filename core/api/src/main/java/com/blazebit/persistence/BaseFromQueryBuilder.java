/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A base interface for builders that support basic query functionality except the SELECT clause.
 * This interface is shared between normal query builders, cte query builders and subquery builders.
 *
 * @param <T> The builder return type
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface BaseFromQueryBuilder<T, X extends BaseFromQueryBuilder<T, X>> extends CommonQueryBuilder<X>, FromBuilder<X>, KeysetQueryBuilder<X>, WhereBuilder<X>, OrderByBuilder<X>, CorrelationQueryBuilder<X>, GroupByBuilder<X>, DistinctBuilder<X>, LimitBuilder<X>, WindowContainerBuilder<X> {

    /**
     * Finishes the query builder.
     *
     * @return The parent query builder
     */
    public T end();
}
