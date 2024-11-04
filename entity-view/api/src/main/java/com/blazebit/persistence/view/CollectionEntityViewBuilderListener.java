/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import java.util.Collection;

/**
 * A listener that adds the built entity view to a collection.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class CollectionEntityViewBuilderListener implements EntityViewBuilderListener {

    private final Collection<Object> collection;

    /**
     * Creates a listener.
     *
     * @param collection The collection to add the built entity view to
     */
    public CollectionEntityViewBuilderListener(Collection<Object> collection) {
        this.collection = collection;
    }

    @Override
    public void onBuildComplete(Object object) {
        collection.add(object);
    }
}
