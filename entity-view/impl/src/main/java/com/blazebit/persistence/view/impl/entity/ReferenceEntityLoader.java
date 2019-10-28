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
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.update.EntityViewUpdaterImpl;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ReferenceEntityLoader extends AbstractEntityLoader {

    public ReferenceEntityLoader(EntityViewManagerImpl evm, ManagedViewType<?> subviewType, ViewToEntityMapper viewIdMapper) {
        super(subviewType.getEntityClass(), jpaIdOf(evm, subviewType), viewIdMapper, evm.getEntityIdAccessor());
    }

    public ReferenceEntityLoader(Class<?> entityClass, javax.persistence.metamodel.SingularAttribute<?, ?> idAttribute, ViewToEntityMapper viewIdMapper, AttributeAccessor entityIdAccessor) {
        super(entityClass, idAttribute, viewIdMapper, entityIdAccessor);
    }

    public static EntityLoader forAttribute(EntityViewManagerImpl evm, ManagedViewType<?> subviewType, AbstractMethodAttribute<?, ?> attribute) {
        return forAttribute(evm, subviewType, attribute.getViewTypes());
    }

    public static EntityLoader forAttribute(EntityViewManagerImpl evm, ManagedViewType<?> subviewType, Set<? extends ManagedViewType<?>> viewTypes) {
        if (viewTypes.size() == 1) {
            return new ReferenceEntityLoader(evm, subviewType, EntityViewUpdaterImpl.createViewIdMapper(evm, subviewType));
        }

        EntityLoader first = null;
        Map<Class<?>, EntityLoader> entityLoaderMap = new HashMap<>(viewTypes.size());
        for (ManagedViewType<?> viewType : viewTypes) {
            ReferenceEntityLoader referenceEntityLoader = new ReferenceEntityLoader(evm, viewType, EntityViewUpdaterImpl.createViewIdMapper(evm, viewType));
            entityLoaderMap.put(viewType.getJavaType(), referenceEntityLoader);
            if (viewType == subviewType) {
                first = referenceEntityLoader;
            }
        }
        return new TargetViewClassBasedEntityLoader(first, entityLoaderMap);
    }

    @Override
    public Object toEntity(UpdateContext context, Object view, Object id) {
        if (id == null) {
            return createEntity();
        }
        EntityManager em = context.getEntityManager();
        return em.getReference(entityClass, getEntityId(context, view, id));
    }

    @Override
    protected Object queryEntity(EntityManager em, Object id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getEntityId(UpdateContext context, Object entity) {
        if (entityIdAccessor == null) {
            return null;
        }

        return entityIdAccessor.getValue(entity);
    }
}
