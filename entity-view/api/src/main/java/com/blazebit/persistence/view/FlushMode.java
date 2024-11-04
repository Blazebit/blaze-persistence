/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

/**
 * The flush mode for an updatable entity view.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public enum FlushMode {

    /**
     * Only dirty attributes are flushed.
     */
    PARTIAL,
    /**
     * If a dirty attribute is encountered will behave like {@link #FULL}, otherwise will only trigger cascades.
     */
    LAZY,
    /**
     * All updatable attributes are flushed. Note that by choosing this mode, dirty tracking is disabled.
     * This has the effect that dirty state access will not work properly.
     */
    FULL;
}
