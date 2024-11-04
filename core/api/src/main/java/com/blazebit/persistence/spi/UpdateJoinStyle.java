/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

/**
 * The possible update join styles.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public enum UpdateJoinStyle {
    /**
     * No support for joins in update statements.
     */
    NONE,
    /**
     * Requires a FROM clause for table references.
     */
    FROM,
    /**
     * Requires a FROM clause for table references but the table alias in the UPDATE clause.
     */
    FROM_ALIAS,
    /**
     * Requires table references to be appended after the update table.
     */
    REFERENCE,
    /**
     * Requires a MERGE statement.
     */
    MERGE;
}