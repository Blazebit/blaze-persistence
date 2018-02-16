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

package com.blazebit.persistence.impl;

import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.metamodel.Attribute;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AssociationFromIdParameterTransformer implements ParameterValueTransformer {

    private static final Map<Class<?>, AssociationFromIdParameterTransformer> INSTANCES = new ConcurrentHashMap<>();

    private final Constructor<Object> entityConstructor;
    private final Field idField;
    private final Method idSetter;

    private AssociationFromIdParameterTransformer(Class<?> associationType, Attribute<?, ?> idAttribute) {
        try {
            Constructor<Object> constructor = (Constructor<Object>) associationType.getDeclaredConstructor();
            constructor.setAccessible(true);
            Member idMember = idAttribute.getJavaMember();

            if (idMember instanceof Method) {
                Method setter = ReflectionUtils.getSetter(associationType, idAttribute.getName());
                setter.setAccessible(true);
                this.idField = null;
                this.idSetter = setter;
            } else if (idMember instanceof Field) {
                Field field = (Field) idMember;
                field.setAccessible(true);
                this.idField = field;
                this.idSetter = null;
            } else {
                throw new IllegalArgumentException("Unsupported attribute member type [" + idMember + "] for attribute [" + idAttribute.getName() + "] of class [" + associationType.getName() + "]");
            }

            this.entityConstructor = constructor;
        } catch (Exception e) {
            throw new IllegalArgumentException("The entity type [" + associationType.getName() + "] does not have a default constructor or id field/setter!", e);
        }
    }

    public static AssociationFromIdParameterTransformer getInstance(Class<?> associationType, Attribute<?, ?> idAttribute) {
        AssociationFromIdParameterTransformer transformer = INSTANCES.get(associationType);
        if (transformer == null) {
            synchronized (INSTANCES) {
                transformer = INSTANCES.get(associationType);
                if (transformer == null) {
                    transformer = new AssociationFromIdParameterTransformer(associationType, idAttribute);
                    INSTANCES.put(associationType, transformer);
                }
            }
        }
        return transformer;
    }

    @Override
    public Object transform(Object originalValue) {
        try {
            Object object = entityConstructor.newInstance();
            if (idField != null) {
                idField.set(object, originalValue);
            } else {
                idSetter.invoke(object, originalValue);
            }
            return object;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not transform parameter value [" + originalValue + "] to entity object of type [" + entityConstructor.getDeclaringClass().getName() + "]", ex);
        }
    }
}
