/*
 * Copyright 2014 - 2021 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.view.impl.entity;

import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.mapper.Mapper;
import com.blazebit.persistence.view.impl.update.EntityViewUpdater;
import com.blazebit.persistence.view.impl.update.EntityViewUpdaterImpl;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.flush.CompositeAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.DirtyAttributeFlusher;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.spi.type.MutableStateTrackable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EmbeddableUpdaterBasedViewToEntityMapper extends AbstractViewToEntityMapper {

    private final Mapper<Object, Object> idViewToEntityMapper;

    public EmbeddableUpdaterBasedViewToEntityMapper(String attributeLocation, EntityViewManagerImpl evm, Class<?> viewTypeClass, Set<Type<?>> readOnlyAllowedSubtypes, Set<Type<?>> persistAllowedSubtypes, Set<Type<?>> updateAllowedSubtypes,
                                                    EntityLoader entityLoader, boolean persistAllowed, Mapper<Object, Object> idViewToEntityMapper, EntityViewUpdaterImpl owner, String ownerMapping, Map<Object, EntityViewUpdaterImpl> localCache) {
        super(attributeLocation, evm, viewTypeClass, readOnlyAllowedSubtypes, persistAllowedSubtypes, updateAllowedSubtypes, entityLoader, null, null, persistAllowed, owner, ownerMapping, localCache);
        this.idViewToEntityMapper = idViewToEntityMapper;
    }

    @Override
    public EntityViewUpdater getUpdater(Object current) {
        Class<?> viewTypeClass = getViewTypeClass(current);
        EntityViewUpdater updater = persistUpdater.get(viewTypeClass);
        if (updater != null) {
            return updater;
        }

        return defaultUpdater;
    }

    @Override
    public <T extends DirtyAttributeFlusher<T, E, V>, E, V> DirtyAttributeFlusher<T, E, V> getNestedDirtyFlusher(UpdateContext context, MutableStateTrackable current, DirtyAttributeFlusher<T, E, V> fullFlusher) {
        if (current == null) {
            return fullFlusher;
        }

        Class<?> viewTypeClass = getViewTypeClass(current);

        EntityViewUpdater updater = persistUpdater.get(viewTypeClass);
        if (updater == null) {
            return null;
        }

        return updater.getNestedDirtyFlusher(context, current, fullFlusher);
    }

    @Override
    public Object applyToEntity(UpdateContext context, Object entity, Object view) {
        if (view == null) {
            return null;
        }

        // Embeddables are always persisted
        Class<?> viewTypeClass = getViewTypeClass(view);
        EntityViewUpdater updater = persistUpdater.get(viewTypeClass);
        if (updater == null) {
            if (view instanceof MutableStateTrackable) {
                if (persistUpdater.isEmpty()) {
                    throw new IllegalStateException("Couldn't update object for attribute '" + attributeLocation + "'. No allowed types for updates found, maybe you forgot to annotate '" + viewTypeClass.getName() + "' with @UpdatableEntityView?");
                } else {
                    throw new IllegalStateException("Couldn't update object for attribute '" + attributeLocation + "'. Expected subviews of the types " + persistUpdater.keySet() + " but got: " + view);
                }
            } else {
                if (entity == null) {
                    entity = entityLoader.toEntity(context, view, null);
                }
                ((CompositeAttributeFlusher) defaultUpdater.getFullGraphNode()).flushEntity(context, entity, null, view, view, null);
                return entity;
            }
        }

        if (entity != null) {
            if (view instanceof MutableStateTrackable) {
                return updater.executePersist(context, entity, (MutableStateTrackable) view);
            } else {
                idViewToEntityMapper.map(view, entity);
                return entity;
            }
        } else {
            if (view instanceof MutableStateTrackable) {
                return updater.executePersist(context, (MutableStateTrackable) view);
            } else {
                entity = entityLoader.toEntity(context, view, null);
                idViewToEntityMapper.map(view, entity);
                return entity;
            }
        }
    }

    public Object createEmbeddable(UpdateContext context) {
        return entityLoader.toEntity(context, null, null);
    }

    @Override
    public Object flushToEntity(UpdateContext context, Object entity, Object view) {
        return applyToEntity(context, entity, view);
    }

    @Override
    public void loadEntities(UpdateContext context, List<Object> views) {
        for (int i = 0; i < views.size(); i++) {
            views.set(i, null);
        }
    }

    @Override
    public Object loadEntity(UpdateContext context, Object view) {
        return null;
    }

    @Override
    public AttributeAccessor getViewIdAccessor() {
        return null;
    }

    @Override
    public AttributeAccessor getEntityIdAccessor() {
        return null;
    }
}
