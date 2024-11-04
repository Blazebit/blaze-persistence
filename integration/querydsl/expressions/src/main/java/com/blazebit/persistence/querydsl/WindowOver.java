/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.dsl.SimpleOperation;

/**
 * {@code WindowOver} is the first part of a WindowFunction construction.
 * Analog to {@link com.querydsl.sql.WindowOver}.
 *
 * @param <T> The window expression result type
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
public class WindowOver<T> extends SimpleOperation<T> {

    private static final long serialVersionUID = -5537888131291270070L;

    /**
     * Create a new window operation.
     *
     * @param type window expression result type
     * @param op operator
     */
    public WindowOver(Class<? extends T> type, Operator op) {
        super(type, op);
    }

    /**
     * Create a new window operation.
     *
     * @param type window expression result type
     * @param op operator
     * @param arg argument
     */
    public WindowOver(Class<? extends T> type, Operator op, Expression<?> arg) {
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
    public WindowOver(Class<? extends T> type, Operator op, Expression<?> arg1, Expression<?> arg2) {
        super(type, op, arg1, arg2);
    }

    /**
     * Start an {@code OVER} clause builder.
     *
     * @return the {@code OVER} clause builder for this window operation
     */
    public WindowFunction<T> over() {
        return new WindowFunction<T>(mixin);
    }

    /**
     * Start an {@code OVER} clause builder with a named window.
     *
     * @param baseWindow Base window definition to extend
     * @return the {@code OVER} clause builder for this window operation
     */
    public WindowFunction<T> over(NamedWindow baseWindow) {
        return new WindowFunction<T>(mixin, baseWindow.getAlias());
    }

}
