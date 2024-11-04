/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

/**
 *
 * Defines a window frame exclusion for a window.
 *
 * @author Christian Beikov
 * @since 1.6.4
 */
public enum BlazeWindowFrameExclusion {
    CURRENT_ROW,
    GROUP,
    TIES,
    NO_OTHERS

}
