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
public enum FlushStrategy {

    /**
     * Flushes changes by loading the entity graph and applying changes on the managed objects.
     */
    ENTITY,
    /**
     * Will flush changes via DML statements if possible, otherwise fallback to {@link #ENTITY} strategy.
     */
    QUERY;
}
