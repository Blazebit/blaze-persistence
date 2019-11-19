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

import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.accessor.Accessors;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.SingularAttribute;
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
    protected final boolean primaryKeyId;

    public AbstractEntityLoader(EntityViewManagerImpl evm, Class<?> entityClass, SingularAttribute<?, ?> jpaIdAttribute, SingularAttribute<?, ?> viewIdMappingAttribute, ViewToEntityMapper viewIdMapper, AttributeAccessor entityIdAccessor) {
        this.entityClass = entityClass;
        this.viewIdMapper = viewIdMapper;
        try {
            Constructor<Object> constructor = (Constructor<Object>) entityClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            this.entityConstructor = constructor;
            if (jpaIdAttribute != null) {
                // The view maps an id attribute different from the entity id attribute
                if (viewIdMappingAttribute != null && !viewIdMappingAttribute.getName().equals(jpaIdAttribute.getName())) {
                    this.idAttributeName = viewIdMappingAttribute.getName();
                    this.entityIdAccessor = Accessors.forEntityMapping(evm, entityClass, viewIdMappingAttribute.getName());
                    this.primaryKeyId = false;
                } else {
                    this.idAttributeName = jpaIdAttribute.getName();
                    this.entityIdAccessor = entityIdAccessor;
                    this.primaryKeyId = true;
                }
            } else {
                this.idAttributeName = null;
                this.entityIdAccessor = null;
                this.primaryKeyId = true;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't find required no-arg constructor for entity class: " + entityClass.getName(), e);
        }
    }

    protected static javax.persistence.metamodel.SingularAttribute<?, ?> jpaIdOf(EntityViewManagerImpl evm, ManagedViewType<?> subviewType) {
        if (subviewType instanceof ViewType<?>) {
            return JpaMetamodelUtils.getSingleIdAttribute(evm.getMetamodel().getEntityMetamodel().entity(subviewType.getEntityClass()));
        }
        return null;
    }

    protected static javax.persistence.metamodel.SingularAttribute<?, ?> viewIdMappingOf(EntityViewManagerImpl evm, ManagedViewType<?> subviewType) {
        if (subviewType instanceof ViewType<?>) {
            ExtendedManagedType<?> managedType = evm.getMetamodel().getEntityMetamodel().getManagedType(ExtendedManagedType.class, subviewType.getEntityClass());
            return (SingularAttribute<?, ?>) managedType.getAttributes().get(((MappingAttribute) ((ViewType<?>) subviewType).getIdAttribute()).getMapping()).getAttribute();
        }
        return null;
    }

    @Override
    public Class<?> getEntityClass() {
        return entityClass;
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

    protected final Object getReferenceOrLoad(UpdateContext context, Object view, Object id) {
        id = getEntityId(context, view, id);
        if (primaryKeyId && context.containsEntity(entityClass, id)) {
            return context.getEntityManager().getReference(entityClass, id);
        } else {
            return queryEntity(context.getEntityManager(), id);
        }
    }

    protected final Object getEntityId(UpdateContext context, Object view, Object id) {
        if (viewIdMapper != null) {
            id = viewIdMapper.applyToEntity(context, null, id);
        }
        return id;
    }

    protected abstract Object queryEntity(EntityManager em, Object id);
}
