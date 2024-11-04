/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

/**
 * The strategy to use when an element was removed from the inverse relation.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public enum InverseRemoveStrategy {

    /**
     * Ignores the fact that elements were removed from the inverse relation.
     */
    IGNORE,
    /**
     * Sets the mapped by attribute of removed elements to <code>null</code>.
     */
    SET_NULL,
    /**
     * Actually deletes the removed elements.
     */
    REMOVE;
}
