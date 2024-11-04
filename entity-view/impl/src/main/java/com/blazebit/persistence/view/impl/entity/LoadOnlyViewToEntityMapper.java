/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.entity;

import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.spi.type.MutableStateTrackable;
import com.blazebit.persistence.view.impl.update.EntityViewUpdater;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.flush.DirtyAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.FetchGraphNode;

import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class LoadOnlyViewToEntityMapper implements ViewToEntityMapper {
    protected final EntityLoader entityLoader;
    protected final AttributeAccessor viewIdAccessor;
    protected final AttributeAccessor entityIdAccessor;

    public LoadOnlyViewToEntityMapper(EntityLoader entityLoader, AttributeAccessor viewIdAccessor, AttributeAccessor entityIdAccessor) {
        this.entityLoader = entityLoader;
        this.viewIdAccessor = viewIdAccessor;
        this.entityIdAccessor = viewIdAccessor == null ? null : entityIdAccessor;
    }

    @Override
    public FetchGraphNode<?> getFullGraphNode() {
        return null;
    }

    @Override
    public DirtyAttributeFlusher<?, ?, ?> getIdFlusher() {
        return null;
    }

    @Override
    public EntityViewUpdater getUpdater(Object current) {
        return null;
    }

    @Override
    public void remove(UpdateContext context, Object element) {

    }

    @Override
    public void removeById(UpdateContext context, Object id) {

    }

    @Override
    public boolean cascades(Object value) {
        return false;
    }

    @Override
    public <T extends DirtyAttributeFlusher<T, E, V>, E, V> DirtyAttributeFlusher<T, E, V> getNestedDirtyFlusher(UpdateContext context, MutableStateTrackable current, DirtyAttributeFlusher<T, E, V> fullFlusher) {
        return fullFlusher;
    }

    @Override
    public Query createUpdateQuery(UpdateContext context, MutableStateTrackable view, DirtyAttributeFlusher<?, ?, ?> nestedGraphNode) {
        return null;
    }

    @Override
    public Object applyToEntity(UpdateContext context, Object entity, Object view) {
        return loadEntity(context, view);
    }

    @Override
    public void applyAll(UpdateContext context, List<Object> elements) {
        loadEntities(context, elements);
    }

    @Override
    public Object flushToEntity(UpdateContext context, Object entity, Object view) {
        return loadEntity(context, view);
    }

    @Override
    public void loadEntities(UpdateContext context, List<Object> views) {
        List<Object> ids = new ArrayList<>(views.size());
        if (viewIdAccessor == null) {
            for (int i = 0; i < views.size(); i++) {
                views.set(i, loadEntity(context, views.get(i)));
            }
        } else {
            for (int i = 0; i < views.size(); i++) {
                ids.add(viewIdAccessor.getValue(views.get(i)));
            }
            entityLoader.toEntities(context, views, ids);
        }
    }

    @Override
    public Object loadEntity(UpdateContext context, Object view) {
        if (view == null) {
            return null;
        }
        Object id = null;
        if (viewIdAccessor != null) {
            id = viewIdAccessor.getValue(view);
        }
        return entityLoader.toEntity(context, view, id);
    }

    @Override
    public AttributeAccessor getViewIdAccessor() {
        return viewIdAccessor;
    }

    @Override
    public AttributeAccessor getEntityIdAccessor() {
        return entityIdAccessor;
    }
}
