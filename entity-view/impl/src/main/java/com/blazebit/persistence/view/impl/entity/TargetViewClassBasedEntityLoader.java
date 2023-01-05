/*
 * Copyright 2014 - 2023 Blazebit.
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

import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class TargetViewClassBasedEntityLoader implements EntityLoader {

    private final EntityLoader first;
    private final Map<Class<?>, EntityLoader> entityLoaderMap;

    public TargetViewClassBasedEntityLoader(EntityLoader first, Map<Class<?>, EntityLoader> entityLoaderMap) {
        this.first = first;
        this.entityLoaderMap = entityLoaderMap;
    }

    @Override
    public Class<?> getEntityClass() {
        return first.getEntityClass();
    }

    @Override
    public void toEntities(UpdateContext context, List<Object> views, List<Object> ids) {
        // TODO: Segment the views
        for (int i = 0; i < views.size(); i++) {
            views.set(i, toEntity(context, views.get(i), ids.get(i)));
        }
    }

    @Override
    public Object toEntity(UpdateContext context, Object view, Object id) {
        if (view instanceof EntityViewProxy) {
            EntityLoader entityLoader = entityLoaderMap.get(((EntityViewProxy) view).$$_getEntityViewClass());
            return entityLoader.toEntity(context, view, id);
        }
        return first.toEntity(context, view, id);
    }

    @Override
    public Object getEntityId(UpdateContext context, Object entity) {
        return first.getEntityId(context, entity);
    }
}
