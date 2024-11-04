/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;


import javax.persistence.EntityManager;

/**
 * A base interface for builders that support normal query functionality.
 * This interface is shared between the criteria builder and paginated criteria builder.
 *
 * @param <T> The query result type
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface QueryBuilder<T, X extends QueryBuilder<T, X>> extends BaseQueryBuilder<T, X>, Queryable<T, X> {

    /**
     * Returns the associated entity manager.
     *
     * @return The associated entity manager
     * @since 1.2.0
     */
    public EntityManager getEntityManager();
}
