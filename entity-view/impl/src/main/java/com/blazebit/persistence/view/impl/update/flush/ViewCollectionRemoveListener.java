/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.collection.CollectionRemoveListener;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.update.UpdateContext;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ViewCollectionRemoveListener implements CollectionRemoveListener {

    private final ViewToEntityMapper viewToEntityMapper;

    public ViewCollectionRemoveListener(ViewToEntityMapper viewToEntityMapper) {
        this.viewToEntityMapper = viewToEntityMapper;
    }

    @Override
    public void onEntityCollectionRemove(UpdateContext context, Object element) {
        Object viewId = ((CompositeAttributeFlusher) viewToEntityMapper.getFullGraphNode()).createViewIdByEntityId(viewToEntityMapper.getEntityIdAccessor().getValue(element));
        viewToEntityMapper.removeById(context, viewId);
    }

    @Override
    public void onCollectionRemove(UpdateContext context, Object element) {
        viewToEntityMapper.remove(context, element);
    }

}
