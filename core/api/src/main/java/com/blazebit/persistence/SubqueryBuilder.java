/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;


/**
 * A builder for subquery criteria queries.
 *
 * @param <T> The parent query builder type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface SubqueryBuilder<T> extends BaseSubqueryBuilder<SubqueryBuilder<T>>, SetOperationBuilder<LeafOngoingSetOperationSubqueryBuilder<T>, StartOngoingSetOperationSubqueryBuilder<T, LeafOngoingFinalSetOperationSubqueryBuilder<T>>>, BaseFromQueryBuilder<T, SubqueryBuilder<T>> {

    /**
     * Finishes the subquery builder.
     *
     * @return The parent query builder
     */
    public T end();

}
