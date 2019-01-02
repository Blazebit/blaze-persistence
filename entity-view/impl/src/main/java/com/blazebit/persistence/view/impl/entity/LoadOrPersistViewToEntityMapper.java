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
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.metamodel.Type;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class LoadOrPersistViewToEntityMapper extends AbstractViewToEntityMapper {

    public LoadOrPersistViewToEntityMapper(String attributeLocation, EntityViewManagerImpl evm, Class<?> viewTypeClass, Set<Type<?>> readOnlyAllowedSubtypes, Set<Type<?>> persistAllowedSubtypes, Set<Type<?>> updateAllowedSubtypes, EntityLoader entityLoader, AttributeAccessor viewIdAccessor, boolean persistAllowed, EntityViewUpdaterImpl owner, String ownerMapping) {
        super(attributeLocation, evm, viewTypeClass, readOnlyAllowedSubtypes, persistAllowedSubtypes, updateAllowedSubtypes, entityLoader, viewIdAccessor, persistAllowed, owner, ownerMapping);
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

        if (viewIdAccessor != null) {
            Object id = viewIdAccessor.getValue(view);

            if (shouldPersist(view, id)) {
                return persist(context, entity, view);
            }

            Class<?> viewTypeClass = getViewTypeClass(view);
            // If the view is read only, just skip to loading
            if (!viewTypeClasses.contains(viewTypeClass)) {
                // If not, check if it was persisted before
                if (persistAllowed && persistUpdater.containsKey(viewTypeClass) && !((EntityViewProxy) view).$$_isNew()) {
                    // If that create view object was previously persisted, we won't persist it again, nor update, but just load it
                } else {
                    throw new IllegalArgumentException("Couldn't load entity object for attribute '" + attributeLocation + "'. Expected subview of one of the types " + names(viewTypeClasses) + " but got: " + view);
                }
            }
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
        return entityLoader.toEntity(context, id);
    }
}
