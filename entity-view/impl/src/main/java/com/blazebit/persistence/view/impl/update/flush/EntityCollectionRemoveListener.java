/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.collection.CollectionRemoveListener;
import com.blazebit.persistence.view.impl.update.UpdateContext;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntityCollectionRemoveListener implements CollectionRemoveListener {

    public static final CollectionRemoveListener INSTANCE = new EntityCollectionRemoveListener();

    private EntityCollectionRemoveListener() {
    }

    @Override
    public void onEntityCollectionRemove(UpdateContext context, Object element) {
        context.getEntityManager().remove(element);
    }

    @Override
    public void onCollectionRemove(UpdateContext context, Object element) {
        context.getEntityManager().remove(element);
    }

}
