/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Predicate;

/**
 * {@code FilterableWindowOver} is the first part of a WindowFunction construction.
 * Analog to {@link com.querydsl.sql.WindowOver}.
 *
 * @param <T> The window expression result type
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
public class FilterableWindowOver<T> extends WindowOver<T> {

    private static final long serialVersionUID = 6327480857012790191L;

    /**
     * Create a new window operation.
     *
     * @param type window expression result type
     * @param op operator
     */
    public FilterableWindowOver(Class<? extends T> type, Operator op) {
        super(type, op);
    }

    /**
     * Create a new window operation.
     *
     * @param type window expression result type
     * @param op operator
     * @param arg argument
     */
    public FilterableWindowOver(Class<? extends T> type, Operator op, Expression<?> arg) {
        super(type, op, arg);
    }

    /**
     * Create a new window operation.
     *
     * @param type window expression result type
     * @param op operator
     * @param arg1 argument
     * @param arg2 argument
     */
    public FilterableWindowOver(Class<? extends T> type, Operator op, Expression<?> arg1, Expression<?> arg2) {
        super(type, op, arg1, arg2);
    }

    /**
     * Filter the values for this aggregate function.
     *
     * @param predicate Predicate to filter by
     * @return The filtered aggregate function
     */
    public WindowOver<T> filter(Predicate predicate) {
        return new WindowOver<T>(getType(), JPQLNextOps.FILTER, mixin, predicate);
    }

}
