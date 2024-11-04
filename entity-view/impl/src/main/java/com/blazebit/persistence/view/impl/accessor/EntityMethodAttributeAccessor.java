/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.accessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntityMethodAttributeAccessor implements AttributeAccessor {

    private final Method getter;
    private final Method setter;
    private final Constructor<?> targetTypeConstructor;

    EntityMethodAttributeAccessor(Method getter, Method setter) {
        this(getter, setter, null);
    }

    EntityMethodAttributeAccessor(Method getter, Method setter, Class<?> targetType) {
        getter.setAccessible(true);
        setter.setAccessible(true);
        this.getter = getter;
        this.setter = setter;
        if (targetType == null) {
            this.targetTypeConstructor = null;
        } else {
            try {
                Constructor<?> declaredConstructor = targetType.getDeclaredConstructor();
                declaredConstructor.setAccessible(true);
                this.targetTypeConstructor = declaredConstructor;
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("The following type is missing a default constructor: " + targetType.getName());
            }
        }
    }

    @Override
    public void setValue(Object entity, Object value) {
        try {
            setter.invoke(entity, value);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't map value [" + value + "] to entity attribute!", e);
        }
    }

    @Override
    public Object getOrCreateValue(Object entity) {
        if (entity == null) {
            return null;
        }

        try {
            Object result = getter.invoke(entity);
            if (result == null && targetTypeConstructor != null) {
                result = targetTypeConstructor.newInstance();
                setValue(entity, result);
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Couldn't access entity attribute value!", e);
        }
    }

    @Override
    public Object getValue(Object entity) {
        if (entity == null) {
            return null;
        }

        try {
            return getter.invoke(entity);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't access entity attribute value!", e);
        }
    }
}
