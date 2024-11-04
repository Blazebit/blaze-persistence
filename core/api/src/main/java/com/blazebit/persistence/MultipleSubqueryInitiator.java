/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * An interface used to create subquery builders for expressions with multiple subqueries.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface MultipleSubqueryInitiator<T> {

    /**
     * Returns the parent query builder.
     *
     * @return The parent query builder
     * @since 1.3.0
     */
    public CommonQueryBuilder<?> getParentQueryBuilder();

    /**
     * Starts a {@link SubqueryInitiator} for the given subquery alias.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<MultipleSubqueryInitiator<T>> with(String subqueryAlias);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the given subquery alias.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     */
    public SubqueryBuilder<MultipleSubqueryInitiator<T>> with(String subqueryAlias, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Finishes the subquery builder.
     *
     * @return The parent query builder
     */
    public T end();
}
