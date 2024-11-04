/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
public enum WindowFrameExclusionType {
    EXCLUDE_CURRENT_ROW,
    EXCLUDE_GROUP,
    EXCLUDE_TIES,
    EXCLUDE_NO_OTHERS
}
