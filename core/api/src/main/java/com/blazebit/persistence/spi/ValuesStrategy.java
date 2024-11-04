/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

/**
 * Strategies for generating a VALUES table reference.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public enum ValuesStrategy {
    VALUES,
    SELECT_VALUES,
    SELECT_UNION;

    // NOTE: another possible strategy would be to use a temporary table
    // CREATE TEMPORARY TABLE IF NOT EXISTS table2 AS (SELECT * FROM table1)
}
