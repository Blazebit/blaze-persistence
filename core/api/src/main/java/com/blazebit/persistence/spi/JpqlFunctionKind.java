/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

/**
 * The funciton kind.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public enum JpqlFunctionKind {
    /**
     * An aggregate function.
     */
    AGGREGATE,
    /**
     * An ordered set aggregate function.
     */
    ORDERED_SET_AGGREGATE,
    /**
     * A window function.
     */
    WINDOW,
    /**
     * A non-deterministic function.
     */
    VOLATILE,
    /**
     * A deterministic function.
     */
    DETERMINISTIC;
}
