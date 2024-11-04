/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.entity;

import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.spi.type.MutableStateTrackable;
import com.blazebit.persistence.view.impl.update.EntityViewUpdater;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.UpdateQueryFactory;
import com.blazebit.persistence.view.impl.update.flush.DirtyAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.FetchGraphNode;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ViewToEntityMapper extends ElementToEntityMapper, UpdateQueryFactory {

    public FetchGraphNode<?> getFullGraphNode();

    public DirtyAttributeFlusher<?, ?, ?> getIdFlusher();

    public <T extends DirtyAttributeFlusher<T, E, V>, E, V> DirtyAttributeFlusher getNestedDirtyFlusher(UpdateContext context, MutableStateTrackable current, DirtyAttributeFlusher<T, E, V> fullFlusher);

    public AttributeAccessor getViewIdAccessor();

    public AttributeAccessor getEntityIdAccessor();

    public EntityViewUpdater getUpdater(Object view);

    public Object flushToEntity(UpdateContext context, Object entity, Object view);

    public Object loadEntity(UpdateContext context, Object view);

    public void loadEntities(UpdateContext context, List<Object> views);

    public boolean cascades(Object value);
}