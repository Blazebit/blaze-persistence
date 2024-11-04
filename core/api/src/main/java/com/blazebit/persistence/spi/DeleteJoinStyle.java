/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

/**
 * The possible delete join styles.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public enum DeleteJoinStyle {
    /**
     * No support for joins in delete statements.
     */
    NONE,
    /**
     * Requires a USING clause for table references.
     */
    USING,
    /**
     * Requires a FROM clause for table references.
     */
    FROM,
    /**
     * Requires a MERGE statement.
     */
    MERGE;
}