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
public interface PluralDirtyChecker<E, V> extends DirtyChecker<E> {

    public DirtyChecker<V> getElementDirtyChecker(V element);
}
