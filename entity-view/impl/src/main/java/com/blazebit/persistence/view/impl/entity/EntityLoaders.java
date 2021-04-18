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

import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.update.EntityViewUpdaterImpl;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
public final class EntityLoaders {

    private EntityLoaders() {
    }

    public static EntityLoader referenceLoaderForAttribute(EntityViewManagerImpl evm, Map<Object, EntityViewUpdaterImpl> localCache, ManagedViewType<?> subviewType, AbstractMethodAttribute<?, ?> attribute) {
        return referenceLoaderForAttribute(evm, localCache, subviewType, attribute.getViewTypes(), null);
    }

    public static EntityLoader referenceLoaderForAttribute(EntityViewManagerImpl evm, Map<Object, EntityViewUpdaterImpl> localCache, ManagedViewType<?> subviewType, Set<? extends ManagedViewType<?>> viewTypes, String attributeIdAttributeName) {
        if (viewTypes.size() == 1) {
            return buildReferenceEntityLoader(evm, localCache, subviewType, attributeIdAttributeName);
        }

        EntityLoader first = null;
        Map<Class<?>, EntityLoader> entityLoaderMap = new HashMap<>(viewTypes.size());
        for (ManagedViewType<?> viewType : viewTypes) {
            EntityLoader referenceEntityLoader = buildReferenceEntityLoader(evm, localCache, viewType, attributeIdAttributeName);
            entityLoaderMap.put(viewType.getJavaType(), referenceEntityLoader);
            if (viewType == subviewType) {
                first = referenceEntityLoader;
            }
        }
        return new TargetViewClassBasedEntityLoader(first, entityLoaderMap);
    }

    private static EntityLoader buildReferenceEntityLoader(EntityViewManagerImpl evm, Map<Object, EntityViewUpdaterImpl> localCache, ManagedViewType<?> subviewType, String attributeIdAttributeName) {
        boolean forceQuery = false;
        if (subviewType instanceof ViewType<?> && attributeIdAttributeName != null) {
            MethodAttribute<?, ?> idAttribute = ((ViewType<?>) subviewType).getIdAttribute();
            if (!attributeIdAttributeName.equals(((MappingAttribute<?, ?>) idAttribute).getMapping())) {
                // If the target attribute id name doesn't match the view id attribute mapping or the entity primary key
                // and the target attribute id uses field access although the JPA provider doesn't support this,
                // we need a special loader that always runs a query
                ExtendedManagedType<?> managedType = evm.getMetamodel().getEntityMetamodel().getManagedType(ExtendedManagedType.class, subviewType.getEntityClass());
                if (managedType.getAttribute(attributeIdAttributeName).getAttribute().getJavaMember() instanceof Field) {
                    forceQuery = !evm.getJpaProvider().supportsProxyParameterForNonPkAssociation();
                }
            }
        }
        return new ReferenceEntityLoader(
            evm,
            subviewType.getEntityClass(),
            AbstractEntityLoader.jpaIdOf(evm, subviewType),
            AbstractEntityLoader.viewIdMappingOf(evm, subviewType),
            EntityViewUpdaterImpl.createViewIdMapper(evm, localCache, subviewType),
            evm.getEntityIdAccessor(),
            forceQuery
        );
    }
}
