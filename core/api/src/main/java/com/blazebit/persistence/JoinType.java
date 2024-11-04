/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * The different join types which are possible.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public enum JoinType {

    /**
     * Inner join.
     */
    INNER,
    /**
     * Left outer join.
     */
    LEFT,
    /**
     * Right outer join.
     */
    RIGHT,
    /**
     * Full outer join.
     */
    FULL;
}
