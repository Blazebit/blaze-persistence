/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.collection.CollectionRemoveListener;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PostFlushCollectionElementDeleter implements PostFlushDeleter {

    private final CollectionRemoveListener collectionRemoveListener;
    private final List<Object> elements;

    public PostFlushCollectionElementDeleter(CollectionRemoveListener collectionRemoveListener, List<Object> elements) {
        this.collectionRemoveListener = collectionRemoveListener;
        this.elements = elements;
    }

    @Override
    public void execute(UpdateContext context) {
        for (Object element : elements) {
            collectionRemoveListener.onCollectionRemove(context, element);
        }
    }
}
