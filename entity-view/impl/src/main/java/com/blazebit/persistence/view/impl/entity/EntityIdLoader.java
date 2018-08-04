/*
 * Copyright 2014 - 2018 Blazebit.
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

import java.lang.reflect.Constructor;

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
    public Object getEntityId(UpdateContext context, Object entity) {
        return entity;
    }

    @Override
    public Object toEntity(UpdateContext context, Object id) {
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
