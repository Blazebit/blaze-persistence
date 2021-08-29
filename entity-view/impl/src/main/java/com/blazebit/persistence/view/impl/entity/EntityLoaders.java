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
import com.blazebit.persistence.view.impl.accessor.Accessors;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.update.EntityViewUpdaterImpl;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;

import javax.persistence.metamodel.BasicType;
import javax.persistence.metamodel.SingularAttribute;
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
        SingularAttribute<?, ?> attributeIdAttribute;
        ViewToEntityMapper viewIdMapper;
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
                if (Accessors.forEntityMappingAsViewAccessor(evm, subviewType, attributeIdAttributeName, true) == null) {
                    // If no attribute on the subview exists for the attribute id attribute, we have to fallback to the primary key and force a query lookup
                    attributeIdAttribute = AbstractEntityLoader.viewIdMappingOf(evm, subviewType);
                    viewIdMapper = EntityViewUpdaterImpl.createViewIdMapper(evm, localCache, subviewType);
                    forceQuery = !evm.getJpaProvider().supportsProxyParameterForNonPkAssociation();
                } else {
                    attributeIdAttribute = AbstractEntityLoader.associationIdMappingOf(evm, subviewType, attributeIdAttributeName);
                    if (attributeIdAttribute.getType() instanceof BasicType<?>) {
                        viewIdMapper = null;
                    } else {
                        throw new UnsupportedOperationException("Composite or association based natural keys for associations are not yet supported!");
                    }
                }
            } else {
                attributeIdAttribute = AbstractEntityLoader.viewIdMappingOf(evm, subviewType);
                viewIdMapper = EntityViewUpdaterImpl.createViewIdMapper(evm, localCache, subviewType);
            }
        } else {
            attributeIdAttribute = AbstractEntityLoader.viewIdMappingOf(evm, subviewType);
            viewIdMapper = EntityViewUpdaterImpl.createViewIdMapper(evm, localCache, subviewType);
        }
        return new ReferenceEntityLoader(
            evm,
            subviewType.getEntityClass(),
            AbstractEntityLoader.jpaIdOf(evm, subviewType),
            attributeIdAttribute,
            viewIdMapper,
            evm.getEntityIdAccessor(),
            forceQuery
        );
    }
}
