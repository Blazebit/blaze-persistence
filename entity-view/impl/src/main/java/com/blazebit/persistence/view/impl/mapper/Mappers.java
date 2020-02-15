/*
 * Copyright 2014 - 2020 Blazebit.
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

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.MacroConfigurationExpressionFactory;
import com.blazebit.persistence.view.impl.accessor.Accessors;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.entity.EntityTupleizer;
import com.blazebit.persistence.view.impl.macro.MutableEmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.impl.macro.MutableViewJpqlMacro;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.update.DefaultEntityTupleizer;
import com.blazebit.persistence.view.metamodel.BasicType;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.metamodel.ViewType;

import java.util.ArrayList;
import java.util.Collections;
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

    public static <S, T> Mapper<S, T> targetViewClassBasedMapper(Map<Class<?>, Mapper<S, T>> mapperMap) {
        return new TargetViewClassBasedMapper<>(mapperMap);
    }

    public static <S, T> Mapper<S, T> forAccessor(AttributeAccessor accessor) {
        return new SimpleMapper<>(accessor);
    }

    public static <S, T> Mapper<S, T> forEntityAttributeMapping(EntityViewManagerImpl evm, Class<S> sourceEntityClass, Class<T> targetEntityClass, Map<String, String> mapping) {
        List<AttributeAccessor> source = new ArrayList<>(mapping.size());
        List<AttributeAccessor> target = new ArrayList<>(mapping.size());
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            source.add(Accessors.forEntityMapping(evm, sourceEntityClass, entry.getKey()));
            target.add(Accessors.forEntityMapping(evm, targetEntityClass, entry.getValue()));
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
        buildViewToEntityMappings("", "", attributes, mappings);
        return forViewToEntityAttributeMapping(evm, sourceViewType, targetEntityClass, mappings);
    }

    @SuppressWarnings("unchecked")
    private static void buildViewToEntityMappings(String attributePrefix, String mappingPrefix, Set<MethodAttribute<?, ?>> attributes, Map<String, String> mappings) {
        for (MethodAttribute<?, ?> attribute : attributes) {
            if (!(attribute instanceof SingularAttribute<?, ?>)) {
                throw new IllegalArgumentException("Plural attributes aren't supported yet for view to entity mappings!");
            }
            SingularAttribute<?, ?> attr = (SingularAttribute<?, ?>) attribute;
            if (attr.getType() instanceof BasicType<?>) {
                mappings.put(attributePrefix + attribute.getName(), mappingPrefix + ((MappingAttribute<?, ?>) attr).getMapping());
            } else {
                ManagedViewType<?> viewType = (ManagedViewType<?>) attr.getType();
                buildViewToEntityMappings(
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
            target.add(Accessors.forEntityMapping(evm, targetEntityClass, entry.getValue()));
        }

        return new AttributeMapper<>(source, target);
    }

    public static <S, T> Mapper<S, T> forEntityAttributeMappingConvertToViewAttributeMapping(EntityViewManagerImpl evm, Class<S> sourceEntityClass, ManagedViewType<T> targetViewType, Map<String, String> mapping) {
        List<AttributeAccessor> source = new ArrayList<>(mapping.size());
        List<AttributeAccessor> target = new ArrayList<>(mapping.size());
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            source.add(Accessors.forEntityMapping(evm, sourceEntityClass, entry.getKey()));
            AttributeAccessor attributeAccessor = Accessors.forEntityMappingAsViewAccessor(evm, targetViewType, entry.getValue(), false);
            if (attributeAccessor == null) {
                return null;
            }
            target.add(attributeAccessor);
        }

        return new AttributeMapper<>(source, target);
    }

    public static <S, T> Mapper<S, T> forViewConvertToViewAttributeMapping(EntityViewManagerImpl evm, ViewType<S> sourceViewType, ViewType<T> targetViewType, String mappedBy, Mapper<S, T> additionalMapper) {
        List<Mapper<S, T>> mappers = new ArrayList<>();
        AttributeAccessor entityAccessor = Accessors.forEntityMapping(
                evm,
                sourceViewType.getEntityClass(),
                ((MappingAttribute<?, ?>) sourceViewType.getIdAttribute()).getMapping()
        );
        ExpressionFactory ef = evm.getCriteriaBuilderFactory().getService(ExpressionFactory.class);
        for (MethodAttribute<?, ?> attribute : targetViewType.getAttributes()) {
            if (attribute.isUpdatable() && attribute instanceof MappingAttribute<?, ?> && attribute instanceof SingularAttribute<?, ?>) {
                if (mappedBy.equals(((MappingAttribute) attribute).getMapping())) {
                    ViewType<?> attributeType = (ViewType<?>) ((SingularAttribute<?, ?>) attribute).getType();
                    Type<?> attributeViewIdType = ((SingularAttribute<?, ?>) attributeType.getIdAttribute()).getType();
                    EntityTupleizer entityTupleizer = null;
                    ObjectBuilder<?> idViewBuilder = null;
                    if (attributeViewIdType instanceof ManagedViewType<?>) {
                        entityTupleizer = new DefaultEntityTupleizer(evm, (ManagedViewType<?>) attributeViewIdType);
                        idViewBuilder = (ObjectBuilder<Object>) evm.getTemplate(
                                new MacroConfigurationExpressionFactory(ef, ef.getDefaultMacroConfiguration()),
                                (ManagedViewTypeImplementor<?>) attributeViewIdType,
                                null,
                                null,
                                new MutableViewJpqlMacro(),
                                null,
                                new MutableEmbeddingViewJpqlMacro(),
                                0
                        ).createObjectBuilder(null, null, null, 0, false, false);
                    }
                    mappers.add(new ReferenceViewAttributeMapper<S, T>(evm, entityAccessor, attributeType.getJavaType(), entityTupleizer, Accessors.forMutableViewAttribute(evm, attribute), idViewBuilder));
                }
            }
        }

        if (mappers.isEmpty()) {
            return additionalMapper;
        }

        if (additionalMapper != null) {
            mappers.add(additionalMapper);
        }
        return new CompositeMapper<>(mappers.toArray(new Mapper[mappers.size()]));
    }

    public static <S, T> Mapper<S, T> forViewConvertToViewAttributeMapping(EntityViewManagerImpl evm, ViewType<S> sourceViewType, ViewType<T> targetViewType, Map<String, String> mapping, Mapper<S, T> additionalMapper) {
        List<Mapper<S, T>> mappers = new ArrayList<>();
        ExpressionFactory ef = evm.getCriteriaBuilderFactory().getService(ExpressionFactory.class);
        for (MethodAttribute<?, ?> attribute : targetViewType.getAttributes()) {
            if (attribute.isUpdatable() && attribute instanceof MappingAttribute<?, ?> && attribute instanceof SingularAttribute<?, ?>) {
                for (Map.Entry<String, String> entry : mapping.entrySet()) {
                    if (entry.getValue().equals(((MappingAttribute) attribute).getMapping())) {
                        Type<?> attributeType = ((SingularAttribute<?, ?>) attribute).getType();
                        AttributeAccessor entityAccessor;
                        if (entry.getKey().isEmpty()) {
                            entityAccessor = Accessors.forEntityMapping(
                                    evm,
                                    sourceViewType.getEntityClass(),
                                    ((MappingAttribute<?, ?>) sourceViewType.getIdAttribute()).getMapping()
                            );
                        } else {
                            entityAccessor = Accessors.forEntityMapping(
                                    evm,
                                    sourceViewType.getEntityClass(),
                                    entry.getKey()
                            );
                        }
                        AttributeAccessor targetAttributeAccessor = Accessors.forMutableViewAttribute(evm, attribute);
                        if (attributeType instanceof ViewType<?>) {
                            ViewType<?> viewType = (ViewType<?>) attributeType;
                            Type<?> attributeViewIdType = ((SingularAttribute<?, ?>) viewType.getIdAttribute()).getType();
                            EntityTupleizer entityTupleizer = null;
                            ObjectBuilder<?> idViewBuilder = null;
                            if (attributeViewIdType instanceof ManagedViewType<?>) {
                                entityTupleizer = new DefaultEntityTupleizer(evm, (ManagedViewType<?>) attributeViewIdType);
                                idViewBuilder = (ObjectBuilder<Object>) evm.getTemplate(
                                        new MacroConfigurationExpressionFactory(ef, ef.getDefaultMacroConfiguration()),
                                        (ManagedViewTypeImplementor<?>) attributeViewIdType,
                                        null,
                                        null,
                                        new MutableViewJpqlMacro(),
                                        null,
                                        new MutableEmbeddingViewJpqlMacro(),
                                        0
                                ).createObjectBuilder(null, null, null, 0, false, false);
                            }
                            mappers.add(new ReferenceViewAttributeMapper<S, T>(evm, entityAccessor, viewType.getJavaType(), entityTupleizer, targetAttributeAccessor, idViewBuilder));
                        } else {
                            mappers.add((Mapper<S, T>) new AttributeMapper<>(Collections.singletonList(entityAccessor), Collections.singletonList(targetAttributeAccessor)));
                        }
                    }
                }
            }
        }

        if (mappers.isEmpty()) {
            return additionalMapper;
        }

        return new CompositeMapper<>(mappers.toArray(new Mapper[mappers.size()]));
    }

}
