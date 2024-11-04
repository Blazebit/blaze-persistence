/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * An interface for builders that support set operators.
 *
 * @param <X> The concrete builder type
 * @param <Y> The set sub-operation result type
 * @param <Z> The set nesting start type 
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface BaseOngoingSetOperationBuilder<X, Y, Z extends StartOngoingSetOperationBuilder<?, ?, ?>> extends SetOperationBuilder<X, Z> {

    /**
     * Ends the current set operation scope and switches back to the parent query.
     *
     * @return The parent query builder
     */
    public Y endSet();
}
