/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.reflection.ReflectionUtils;

import jakarta.persistence.Query;
import jakarta.persistence.metamodel.Attribute;
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
    public ParameterValueTransformer forQuery(Query query) {
        return this;
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
