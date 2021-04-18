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
import com.blazebit.persistence.view.impl.update.EntityViewUpdaterImpl;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.metamodel.Type;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CreateOnlyViewToEntityMapper extends AbstractViewToEntityMapper {

    public CreateOnlyViewToEntityMapper(String attributeLocation, EntityViewManagerImpl evm, Class<?> viewTypeClass, Set<Type<?>> readOnlyAllowedSubtypes, Set<Type<?>> persistAllowedSubtypes, Set<Type<?>> updateAllowedSubtypes,
                                        EntityLoader entityLoader, AttributeAccessor viewIdAccessor, AttributeAccessor entityIdAccessor, boolean persistAllowed, EntityViewUpdaterImpl owner, String ownerMapping, Map<Object, EntityViewUpdaterImpl> localCache) {
        super(attributeLocation, evm, viewTypeClass, readOnlyAllowedSubtypes, persistAllowedSubtypes, updateAllowedSubtypes, entityLoader, viewIdAccessor, entityIdAccessor, persistAllowed, owner, ownerMapping, localCache);
    }

    @Override
    public Object applyToEntity(UpdateContext context, Object entity, Object view) {
        if (entity == null) {
            return persist(context, entity, view);
        }

        return entity;
    }

    @Override
    public Object flushToEntity(UpdateContext context, Object entity, Object view) {
        if (entity == null) {
            return persist(context, entity, view);
        }

        return entity;
    }

    @Override
    public void loadEntities(UpdateContext context, List<Object> views) {
        for (int i = 0; i < views.size(); i++) {
            views.set(i, persist(context, null, views.get(i)));
        }
    }

    @Override
    public Object loadEntity(UpdateContext context, Object view) {
        return persist(context, null, view);
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
