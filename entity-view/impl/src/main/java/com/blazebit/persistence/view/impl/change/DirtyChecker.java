/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.change;

/**
 * An interface for determining the dirty kind of two objects.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface DirtyChecker<E> {

    public <X> DirtyChecker<X>[] getNestedCheckers(E current);

    /**
     * Returns the dirty kind of the objects.
     *
     * @return The dirty kind
     */
    public DirtyKind getDirtyKind(E initial, E current);

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    enum DirtyKind {
        NONE,
        UPDATED,
        MUTATED;
    }
}
