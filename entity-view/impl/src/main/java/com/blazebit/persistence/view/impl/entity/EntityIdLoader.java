/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.entity;

import com.blazebit.persistence.view.impl.update.UpdateContext;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class EntityIdLoader implements EntityLoader {

    protected final Constructor<Object> entityIdConstructor;

    public EntityIdLoader(Class<?> entityClass) {
        try {
            Constructor<Object> constructor = (Constructor<Object>) entityClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            this.entityIdConstructor = constructor;
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't find required no-arg constructor for entity id class: " + entityClass.getName(), e);
        }
    }

    @Override
    public Class<?> getEntityClass() {
        return entityIdConstructor.getDeclaringClass();
    }

    @Override
    public Object getEntityId(UpdateContext context, Object entity) {
        return entity;
    }

    @Override
    public void toEntities(UpdateContext context, List<Object> views, List<Object> ids) {
        for (int i = 0; i < views.size(); i++) {
            views.set(i, toEntity(context, views.get(i), ids.get(i)));
        }
    }

    @Override
    public Object toEntity(UpdateContext context, Object view, Object id) {
        if (id == null) {
            try {
                return entityIdConstructor.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Couldn't map entity view to entity!", e);
            }
        }

        return id;
    }
}
