/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public enum FunctionKind {
    AGGREGATE,
    ORDERED_SET_AGGREGATE,
    WINDOW,
    VOLATILE,
    DETERMINISTIC;
}
