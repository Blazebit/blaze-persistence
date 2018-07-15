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

package com.blazebit.persistence.view.impl.mapper;

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.accessor.Accessors;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.metamodel.BasicType;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class Mappers {

    private Mappers() {
    }

    public static <S, T> Mapper<S, T> forAccessor(AttributeAccessor accessor) {
        return new SimpleMapper<>(accessor);
    }

    public static <S, T> Mapper<S, T> forEntityAttributeMapping(EntityMetamodel metamodel, Class<S> sourceEntityClass, Class<T> targetEntityClass, Map<String, String> mapping) {
        List<AttributeAccessor> source = new ArrayList<>(mapping.size());
        List<AttributeAccessor> target = new ArrayList<>(mapping.size());
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            source.add(Accessors.forEntityMapping(metamodel, sourceEntityClass, entry.getKey()));
            target.add(Accessors.forEntityMapping(metamodel, targetEntityClass, entry.getValue()));
        }

        return new AttributeMapper<>(source, target);
    }

    public static <S, T> Mapper<S, T> forViewAttributeMapping(EntityViewManagerImpl evm, ManagedViewType<S> sourceViewType, ManagedViewType<T> targetViewType, Map<String, String> mapping) {
        List<AttributeAccessor> source = new ArrayList<>(mapping.size());
        List<AttributeAccessor> target = new ArrayList<>(mapping.size());
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            source.add(Accessors.forViewAttributePath(evm, sourceViewType, entry.getKey(), true));
            target.add(Accessors.forViewAttributePath(evm, targetViewType, entry.getValue(), false));
        }

        return new AttributeMapper<>(source, target);
    }

    @SuppressWarnings("unchecked")
    public static <S, T> Mapper<S, T> forViewToEntityAttributeMapping(EntityViewManagerImpl evm, ManagedViewType<S> sourceViewType, Class<T> targetEntityClass) {
        Set<MethodAttribute<?, ?>> attributes = (Set<MethodAttribute<?, ?>>) (Set) sourceViewType.getAttributes();
        Map<String, String> mappings = new HashMap<>(attributes.size());
        buildMappings("", "", attributes, mappings);
        return forViewToEntityAttributeMapping(evm, sourceViewType, targetEntityClass, mappings);
    }

    @SuppressWarnings("unchecked")
    private static void buildMappings(String attributePrefix, String mappingPrefix, Set<MethodAttribute<?, ?>> attributes, Map<String, String> mappings) {
        for (MethodAttribute<?, ?> attribute : attributes) {
            if (!(attribute instanceof SingularAttribute<?, ?>)) {
                throw new IllegalArgumentException("Plural attributes aren't supported yet for view to entity mappings!");
            }
            SingularAttribute<?, ?> attr = (SingularAttribute<?, ?>) attribute;
            if (attr.getType() instanceof BasicType<?>) {
                mappings.put(attributePrefix + attribute.getName(), mappingPrefix + ((MappingAttribute<?, ?>) attr).getMapping());
            } else {
                ManagedViewType<?> viewType = (ManagedViewType<?>) attr.getType();
                buildMappings(
                        attributePrefix + attribute.getName() + ".",
                        mappingPrefix + ((MappingAttribute<?, ?>) attr).getMapping() + ".",
                        (Set<MethodAttribute<?, ?>>) (Set) viewType.getAttributes(),
                        mappings
                );
            }
        }
    }

    public static <S, T> Mapper<S, T> forViewToEntityAttributeMapping(EntityViewManagerImpl evm, ManagedViewType<S> sourceViewType, Class<T> targetEntityClass, Map<String, String> mapping) {
        EntityMetamodel metamodel = evm.getCriteriaBuilderFactory().getService(EntityMetamodel.class);
        List<AttributeAccessor> source = new ArrayList<>(mapping.size());
        List<AttributeAccessor> target = new ArrayList<>(mapping.size());
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            source.add(Accessors.forViewAttributePath(evm, sourceViewType, entry.getKey(), true));
            target.add(Accessors.forEntityMapping(metamodel, targetEntityClass, entry.getValue()));
        }

        return new AttributeMapper<>(source, target);
    }

    public static <S, T> Mapper<S, T> forEntityAttributeMappingConvertToViewAttributeMapping(EntityViewManagerImpl evm, Class<S> sourceEntityClass, ManagedViewType<T> targetViewType, Map<String, String> mapping) {
        EntityMetamodel metamodel = evm.getCriteriaBuilderFactory().getService(EntityMetamodel.class);
        List<AttributeAccessor> source = new ArrayList<>(mapping.size());
        List<AttributeAccessor> target = new ArrayList<>(mapping.size());
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            source.add(Accessors.forEntityMapping(metamodel, sourceEntityClass, entry.getKey()));
            target.add(Accessors.forEntityMappingAsViewAccessor(evm, targetViewType, entry.getValue(), false));
        }

        return new AttributeMapper<>(source, target);
    }
}
