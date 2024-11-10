/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.listener;

import com.blazebit.persistence.view.EntityViewManager;

import jakarta.persistence.EntityManager;
import java.lang.reflect.Method;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ViewInstanceListener {

    private final Method listener;
    private final int parameterCount;
    private final int entityViewManagerIndex;
    private final int entityManagerIndex;

    public ViewInstanceListener(Method listener) {
        int entityViewManagerIndex = -1;
        int entityManagerIndex = -1;
        Class<?>[] parameterTypes = listener.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (EntityViewManager.class == parameterTypes[i]) {
                entityViewManagerIndex = i;
            } else if (EntityManager.class == parameterTypes[i]) {
                entityManagerIndex = i;
            } else {
                throw new IllegalArgumentException("Illegal argument at index " + i + " of type " + parameterTypes[i].getName() + " in lifecycle method " + listener.getDeclaringClass().getName() + "." + listener.getName() +
                        "! Allowed argument types are [" + EntityViewManager.class.getSimpleName() + ", " + EntityManager.class.getSimpleName() + "]");
            }
        }
        listener.setAccessible(true);
        this.listener = listener;
        this.parameterCount = parameterTypes.length;
        this.entityViewManagerIndex = entityViewManagerIndex;
        this.entityManagerIndex = entityManagerIndex;
    }

    protected final Object invoke(EntityViewManager entityViewManager, EntityManager entityManager, Object view) {
        Object[] parameters = new Object[parameterCount];
        if (entityViewManagerIndex != -1) {
            parameters[entityViewManagerIndex] = entityViewManager;
        }
        if (entityManagerIndex != -1) {
            parameters[entityManagerIndex] = entityManager;
        }
        try {
            return listener.invoke(view, parameters);
        } catch (Exception e) {
            throw new RuntimeException("Error happened during invocation of the lifecycle method", e);
        }
    }
}
