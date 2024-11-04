/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
