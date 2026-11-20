/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jackson;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.AbstractDeserializer;
import tools.jackson.databind.deser.ValueDeserializerModifier;
import tools.jackson.databind.introspect.AnnotatedMethod;
import tools.jackson.databind.introspect.VisibilityChecker;
import tools.jackson.databind.module.SimpleModule;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class EntityViewAwareObjectMapper {

    private final EntityViewManager entityViewManager;
    private final ObjectMapper objectMapper;

    public EntityViewAwareObjectMapper(final EntityViewManager entityViewManager, final ObjectMapper objectMapper) {
        this(entityViewManager, objectMapper, null);
    }

    public EntityViewAwareObjectMapper(final EntityViewManager entityViewManager, final ObjectMapper objectMapper, final EntityViewIdValueAccessor entityViewIdValueAccessor) {
        this.entityViewManager = entityViewManager;
        SimpleModule module = new SimpleModule("Blaze-Persistence");

        module.setDeserializerModifier(new ValueDeserializerModifier() {
            @Override
            public ValueDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription.Supplier beanDesc, ValueDeserializer<?> deserializer) {
                if (deserializer instanceof AbstractDeserializer) {
                    ManagedViewType<?> view = entityViewManager.getMetamodel().managedView(beanDesc.getBeanClass());
                    if (view != null) {
                        return new EntityViewReferenceDeserializer(
                                entityViewManager,
                                view,
                                objectMapper,
                                beanDesc.get().getIgnoredPropertyNames(),
                                entityViewIdValueAccessor
                        );
                    }
                }
                return deserializer;
            }
        });
        this.objectMapper = objectMapper.rebuild()
                .addModule(module)
                .disable(MapperFeature.INFER_PROPERTY_MUTATORS)
                .changeDefaultVisibility(existing -> new VisibilityChecker(JsonAutoDetect.Visibility.DEFAULT) {
                    @Override
                    public boolean isSetterVisible(AnnotatedMethod method) {
                        if (super.isSetterVisible(method)) {
                            Method member = method.getMember();
                            Class<?> rawParameterType = member.getParameterTypes()[0];
                            if (Collection.class.isAssignableFrom(rawParameterType) || Map.class.isAssignableFrom(rawParameterType)) {
                                return isCollectionSetterVisible(member.getDeclaringClass(), member.getName());
                            } else {
                                return true;
                            }
                        }
                        return false;
                    }

                    private boolean isCollectionSetterVisible(Class<?> declaringClass, String setterName) {
                        final ViewMetamodel metamodel = entityViewManager.getMetamodel();
                        ManagedViewType<?> managedViewType = metamodel.managedView(declaringClass);
                        if (managedViewType == null) {
                            Class<?> superclass = declaringClass.getSuperclass();
                            if (superclass != Object.class) {
                                ManagedViewType<?> managedViewTypeSuper = metamodel.managedView(superclass);
                                if (managedViewTypeSuper != null) {
                                    managedViewType = managedViewTypeSuper;
                                }
                            }
                            if (managedViewType == null) {
                                for (Class<?> interfaceClass : declaringClass.getInterfaces()) {
                                    ManagedViewType<?> managedViewTypeInterface = metamodel.managedView(interfaceClass);
                                    if (managedViewTypeInterface != null) {
                                        managedViewType = managedViewTypeInterface;
                                        break;
                                    }
                                }
                            }
                            if (managedViewType == null) {
                                return true;
                            }
                        }
                        String attributeName = Character.toLowerCase(setterName.charAt(3)) + setterName.substring(4);
                        MethodAttribute<?, ?> attribute = managedViewType.getAttribute(attributeName);
                        return attribute == null || !attribute.isCollection();
                    }
                })
                .build();
    }

    public EntityViewManager getEntityViewManager() {
        return entityViewManager;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public boolean canRead(Class<?> clazz) {
        return entityViewManager.getMetamodel().managedView(clazz) != null;
    }

    public boolean canRead(JavaType javaType) {
        if (!javaType.isContainerType()) {
            return canRead(javaType.getRawClass());
        } else if (javaType.isCollectionLikeType()) {
            return canRead(javaType.getContentType().getRawClass());
        }
        return false;
    }

    public ObjectReader readerFor(JavaType javaType) {
        if (Collection.class.isAssignableFrom(javaType.getRawClass())) {
            return objectMapper.readerFor(javaType);
        } else {
            return readerFor(javaType.getRawClass());
        }
    }

    public ObjectReader readerFor(Class<?> type) {
        return objectMapper.readerFor(type);
    }
}
