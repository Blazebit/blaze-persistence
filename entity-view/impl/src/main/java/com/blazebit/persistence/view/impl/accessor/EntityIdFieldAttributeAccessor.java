/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.accessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import com.blazebit.persistence.spi.JpaProvider;

/**
 * @author Christian Beikov
 * @since 1.6.16
 */
public class EntityIdFieldAttributeAccessor implements AttributeAccessor {

    private final JpaProvider jpaProvider;
    private final Field field;
    private final Constructor<?> targetTypeConstructor;

    EntityIdFieldAttributeAccessor(JpaProvider jpaProvider, Field field, Class<?> targetType) {
        this.jpaProvider = jpaProvider;
        field.setAccessible(true);
        this.field = field;
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
            field.set(jpaProvider.unproxy(entity), value);
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
            Object result = jpaProvider.getIdentifier(entity);
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
            return jpaProvider.getIdentifier(entity);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't access entity attribute value!", e);
        }
    }
}
