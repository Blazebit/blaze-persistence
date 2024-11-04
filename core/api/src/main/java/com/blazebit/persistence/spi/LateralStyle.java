/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

/**
 * The possible lateral styles.
 *
 * @author Christian Beikov
 * @since 1.4.1
 */
public enum LateralStyle {
    /**
     * No support for lateral.
     */
    NONE,
    /**
     * The SQL standard syntax.
     */
    LATERAL,
    /**
     * The APPLY syntax.
     */
    APPLY;
}