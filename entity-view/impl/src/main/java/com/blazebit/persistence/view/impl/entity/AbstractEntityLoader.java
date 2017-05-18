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

import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.ViewType;

import javax.persistence.EntityManager;
import java.lang.reflect.Constructor;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractEntityLoader implements EntityLoader {
    protected final Class<?> entityClass;
    protected final ViewToEntityMapper viewIdMapper;
    protected final Constructor<Object> entityConstructor;
    protected final String idAttributeName;
    protected final AttributeAccessor entityIdAccessor;

    public AbstractEntityLoader(Class<?> entityClass, javax.persistence.metamodel.SingularAttribute<?, ?> jpaIdAttribute, ViewToEntityMapper viewIdMapper, AttributeAccessor entityIdAccessor) {
        this.entityClass = entityClass;
        this.viewIdMapper = viewIdMapper;
        try {
            Constructor<Object> constructor = (Constructor<Object>) entityClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            this.entityConstructor = constructor;
            if (jpaIdAttribute != null) {
                this.idAttributeName = jpaIdAttribute.getName();
                this.entityIdAccessor = entityIdAccessor;
            } else {
                this.idAttributeName = null;
                this.entityIdAccessor = null;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't find required no-arg constructor for entity class: " + entityClass.getName(), e);
        }
    }

    protected static javax.persistence.metamodel.SingularAttribute jpaIdOf(EntityViewManagerImpl evm, ManagedViewType<?> subviewType) {
        if (subviewType instanceof ViewType<?>) {
            return JpaMetamodelUtils.getSingleIdAttribute(evm.getMetamodel().getEntityMetamodel().entity(subviewType.getEntityClass()));
        }
        return null;
    }

    @Override
    public Object getEntityId(UpdateContext context, Object entity) {
        if (entityIdAccessor == null) {
            return null;
        }

        return entityIdAccessor.getValue(entity);
    }

    protected final Object createEntity() {
        try {
            return entityConstructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't map entity view to entity!", e);
        }
    }

    protected final Object getReferenceOrLoad(UpdateContext context, Object id) {
        if (viewIdMapper != null) {
            id = viewIdMapper.applyToEntity(context, null, id);
        }
        if (context.containsEntity(entityClass, id)) {
            return context.getEntityManager().getReference(entityClass, id);
        } else {
            return queryEntity(context.getEntityManager(), id);
        }
    }

    protected abstract Object queryEntity(EntityManager em, Object id);
}
