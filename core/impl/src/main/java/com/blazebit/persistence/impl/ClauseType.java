/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public enum ClauseType {
    SELECT,
    WHERE,
    GROUP_BY,
    ORDER_BY,
    HAVING,
    JOIN,
    SET,
    CTE,
    WINDOW
}
