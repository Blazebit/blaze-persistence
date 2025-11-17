/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jsonb;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.json.bind.config.PropertyVisibilityStrategy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
public class EntityViewPropertyVisibilityStrategy implements PropertyVisibilityStrategy {

    private final EntityViewManager evm;

    public EntityViewPropertyVisibilityStrategy(EntityViewManager evm) {
        this.evm = evm;
    }

    @Override
    public boolean isVisible(Field field) {
        return false;
    }

    @Override
    public boolean isVisible(Method method) {
        if (EntityViewProxy.class.isAssignableFrom(method.getDeclaringClass())) {
            Class<?> superclass = method.getDeclaringClass().getSuperclass();
            if (superclass == Object.class) {
                superclass = method.getDeclaringClass().getInterfaces()[0];
            }
            try {
                method = superclass.getMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Couldn't find method on parent type", e);
            }
        }
        return Modifier.isPublic(method.getModifiers()) && method.getAnnotation(JsonbTransient.class) == null;
    }
}
