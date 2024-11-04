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
public enum WindowFramePositionType {
    UNBOUNDED_PRECEDING,
    BOUNDED_PRECEDING,
    CURRENT_ROW,
    UNBOUNDED_FOLLOWING,
    BOUNDED_FOLLOWING
}
