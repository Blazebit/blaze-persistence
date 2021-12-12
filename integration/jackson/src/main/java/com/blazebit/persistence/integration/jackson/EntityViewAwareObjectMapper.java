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

package com.blazebit.persistence.integration.jackson;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.module.SimpleModule;

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
        SimpleModule module = new SimpleModule();

        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                ManagedViewType<?> view = entityViewManager.getMetamodel().managedView(beanDesc.getBeanClass());
                if (view != null) {
                    return new EntityViewReferenceDeserializer(entityViewManager, view, objectMapper, beanDesc.getIgnoredPropertyNames(), entityViewIdValueAccessor);
                }
                return deserializer;
            }
        });
        objectMapper.registerModule(module);
        // We need this property, otherwise Jackson thinks it can use non-visible setters as mutators
        objectMapper.configure(MapperFeature.INFER_PROPERTY_MUTATORS, false);
        // Obviously, we don't want fields to be set which are final because these are read-only
        objectMapper.configure(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS, false);
        objectMapper.setVisibility(new VisibilityChecker.Std(JsonAutoDetect.Visibility.DEFAULT) {

            // We make setters for collections "invisible" so that a SetterlessProperty is used to always merge values into existing collections

            @Override
            public boolean isSetterVisible(Method m) {
                if (super.isSetterVisible(m)) {
                    Class<?> rawParameterType = m.getParameterTypes()[0];
                    if (Collection.class.isAssignableFrom(rawParameterType) || Map.class.isAssignableFrom(rawParameterType)) {
                        return isCollectionSetterVisible(m.getDeclaringClass(), m.getName());
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
                    // This could be the implementation class, so check the super class
                    Class<?> superclass = declaringClass.getSuperclass();
                    if (superclass != Object.class) {
                        ManagedViewType<?> managedViewTypeSuper = metamodel.managedView(superclass);
                        if (managedViewTypeSuper != null) {
                            managedViewType = managedViewTypeSuper;
                        }
                    }
                    // If it is not, check the interfaces
                    if (managedViewType == null) {
                        for (Class<?> interfaceClass : declaringClass.getInterfaces()) {
                            ManagedViewType<?> managedViewTypeInterface = metamodel.managedView(interfaceClass);
                            if (managedViewTypeInterface != null) {
                                managedViewType = managedViewTypeInterface;
                                break;
                            }
                        }
                    }
                    // If this is not an entity view, the setter is visible
                    if (managedViewType == null) {
                        return true;
                    }
                }
                // If this is an entity view, the setter is only visible if this is a singular attribute
                String attributeName = Character.toLowerCase(setterName.charAt(3)) + setterName.substring(4);
                MethodAttribute<?, ?> attribute = managedViewType.getAttribute(attributeName);
                return attribute == null || !attribute.isCollection();
            }
        });

        this.objectMapper = objectMapper;
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
