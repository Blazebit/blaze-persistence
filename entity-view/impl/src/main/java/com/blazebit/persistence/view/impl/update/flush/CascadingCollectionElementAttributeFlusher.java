/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.flush;

/**
 *
 * @author Christian Beikov
 * @since 1.6.4
 */
public class CascadingCollectionElementAttributeFlusher<E, V> extends CollectionElementAttributeFlusher<E, V> {

    public CascadingCollectionElementAttributeFlusher(DirtyAttributeFlusher<?, E, V> nestedGraphNode, Object element, boolean optimisticLockProtected) {
        super(nestedGraphNode, element, optimisticLockProtected);
    }
}
