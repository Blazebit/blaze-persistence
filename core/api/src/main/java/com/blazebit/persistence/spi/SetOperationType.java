/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

/**
 * The possible set operation types.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public enum SetOperationType {
    /**
     * The UNION set operation.
     */
    UNION,
    /**
     * The UNION ALL set operation.
     */
    UNION_ALL,
    /**
     * The INTERSECT set operation.
     */
    INTERSECT,
    /**
     * The INTERSECT ALL set operation.
     */
    INTERSECT_ALL,
    /**
     * The EXCEPT set operation.
     */
    EXCEPT,
    /**
     * The EXCEPT ALL set operation.
     */
    EXCEPT_ALL;
}