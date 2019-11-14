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

package com.blazebit.persistence.integration.jackson;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class EntityViewAwareObjectMapper {

    private final EntityViewManager entityViewManager;
    private final ObjectMapper objectMapper;

    public EntityViewAwareObjectMapper(final EntityViewManager entityViewManager, ObjectMapper objectMapper) {
        this.entityViewManager = entityViewManager;
        final ViewMetamodel metamodel = entityViewManager.getMetamodel();
        SimpleModule module = new SimpleModule();

        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                ManagedViewType<?> view = metamodel.managedView(beanDesc.getBeanClass());
                if (view != null) {
                    return new EntityViewReferenceDeserializer(entityViewManager, view, (JsonDeserializer<Object>) deserializer);
                }
                return deserializer;
            }
        });
        objectMapper.registerModule(module);
        objectMapper.setVisibility(new VisibilityChecker.Std(JsonAutoDetect.Visibility.DEFAULT) {
            @Override
            public boolean isGetterVisible(Method m) {
                return metamodel.managedView(m.getDeclaringClass()) == null && super.isGetterVisible(m);
            }

            @Override
            public boolean isGetterVisible(AnnotatedMethod m) {
                return metamodel.managedView(m.getDeclaringClass()) == null && super.isGetterVisible(m);
            }

            @Override
            public boolean isIsGetterVisible(Method m) {
                return metamodel.managedView(m.getDeclaringClass()) == null && super.isGetterVisible(m);
            }

            @Override
            public boolean isIsGetterVisible(AnnotatedMethod m) {
                return metamodel.managedView(m.getDeclaringClass()) == null && super.isGetterVisible(m);
            }
        });

        this.objectMapper = objectMapper;
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
