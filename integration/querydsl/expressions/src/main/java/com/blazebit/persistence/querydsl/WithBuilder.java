/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.querydsl;

import com.querydsl.core.types.Expression;

/**
 * {@code WithBuilder} is a builder for common table expressions.
 * Analog to {@link com.querydsl.sql.WithBuilder}.
 *
 * @param <R> Expression result type
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
public interface WithBuilder<R> {

    /**
     * Bind a subquery expression to a CTE declaration
     * @param expr Subquery expression
     * @return query builder
     */
    R as(Expression<?> expr);

}
