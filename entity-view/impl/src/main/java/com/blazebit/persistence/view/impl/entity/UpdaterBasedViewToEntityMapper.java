/*
 * Copyright 2014 - 2019 Blazebit.
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
import com.blazebit.persistence.view.impl.update.EntityViewUpdaterImpl;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;
import com.blazebit.persistence.view.impl.proxy.MutableStateTrackable;
import com.blazebit.persistence.view.impl.update.EntityViewUpdater;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.flush.DirtyAttributeFlusher;
import com.blazebit.persistence.view.metamodel.Type;

import javax.persistence.Query;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class UpdaterBasedViewToEntityMapper extends AbstractViewToEntityMapper {

    public UpdaterBasedViewToEntityMapper(String attributeLocation, EntityViewManagerImpl evm, Class<?> viewTypeClass, Set<Type<?>> readOnlyAllowedSubtypes, Set<Type<?>> persistAllowedSubtypes, Set<Type<?>> updateAllowedSubtypes,
                                          EntityLoader entityLoader, AttributeAccessor viewIdAccessor, AttributeAccessor entityIdAccessor, boolean persistAllowed, EntityViewUpdaterImpl owner, String ownerMapping) {
        super(attributeLocation, evm, viewTypeClass, readOnlyAllowedSubtypes, persistAllowedSubtypes, updateAllowedSubtypes, entityLoader, viewIdAccessor, entityIdAccessor, persistAllowed, owner, ownerMapping);
    }

    @Override
    public EntityViewUpdater getUpdater(Object current) {
        Class<?> viewTypeClass = getViewTypeClass(current);

        Object id = null;
        if (viewIdAccessor != null) {
            id = viewIdAccessor.getValue(current);
        }

        if (shouldPersist(current, id)) {
            if (!persistAllowed) {
                return null;
            }
            return persistUpdater.get(viewTypeClass);
        }

        EntityViewUpdater updater = updateUpdater.get(viewTypeClass);
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

        Object id = null;
        if (viewIdAccessor != null) {
            id = viewIdAccessor.getValue(current);
        }
        Class<?> viewTypeClass = getViewTypeClass(current);

        if (shouldPersist(current, id)) {
            if (!persistAllowed) {
                return null;
            }
            EntityViewUpdater updater = persistUpdater.get(viewTypeClass);
            if (updater == null) {
                throw new IllegalStateException("Couldn't persist object for attribute '" + attributeLocation + "'. Expected subviews of the types " + names(persistUpdater.keySet()) + " but got: " + current);
            }

            return updater.getNestedDirtyFlusher(context, current, fullFlusher);
        }

        EntityViewUpdater updater = updateUpdater.get(viewTypeClass);
        if (updater == null) {
            if (viewTypeClasses.contains(viewTypeClass)) {
                return null;
            }
            throw new IllegalStateException("Couldn't update object for attribute '" + attributeLocation + "'. Expected subviews of the types " + names(updateUpdater.keySet()) + " but got: " + current);
        }

        return updater.getNestedDirtyFlusher(context, current, fullFlusher);
    }

    @Override
    public Object applyToEntity(UpdateContext context, Object entity, Object view) {
        Object object = flushToEntity(context, entity, view);
        if (object == null) {
            return loadEntity(context, view);
        }

        return object;
    }

    @Override
    public Object flushToEntity(UpdateContext context, Object entity, Object view) {
        if (view == null) {
            return null;
        }

        Object id = null;
        if (viewIdAccessor != null) {
            id = viewIdAccessor.getValue(view);
        }

        if (shouldPersist(view, id)) {
            return persist(context, entity, view);
        }

        Class<?> viewTypeClass = getViewTypeClass(view);
        EntityViewUpdater updater = updateUpdater.get(viewTypeClass);
        if (updater == null) {
            if (viewTypeClasses.contains(viewTypeClass)) {
                return null;
            }
            if (persistAllowed && persistUpdater.containsKey(viewTypeClass) && !((EntityViewProxy) view).$$_isNew()) {
                // If that create view object was previously persisted, we won't persist it again, nor update, but just load it
                return null;
            } else {
                throw new IllegalStateException("Couldn't update object for attribute '" + attributeLocation + "'. Expected subviews of the types " + names(persistUpdater.keySet()) + " but got: " + view);
            }
        }

        if (updater.executeUpdate(context, (MutableStateTrackable) view)) {
            return entityLoader.toEntity(context, view, id);
        }

        return null;
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
    public Query createUpdateQuery(UpdateContext context, Object view, DirtyAttributeFlusher<?, ?, ?> nestedGraphNode) {
        Class<?> viewTypeClass = getViewTypeClass(view);
        EntityViewUpdater updater = updateUpdater.get(viewTypeClass);
        if (updater == null) {
            return null;
        }

        // If this is updatable, but also createable we check if this object needs persisting
        // If it does need persisting, we can't possibly do an update query
        if (persistAllowed && persistUpdater.containsKey(viewTypeClass) && ((EntityViewProxy) view).$$_isNew()) {
            return null;
        }

        return updater.createUpdateQuery(context, (MutableStateTrackable) view, nestedGraphNode);
    }
}
