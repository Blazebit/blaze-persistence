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
public class ViewInstanceEntityListener {

    private final Method listener;
    private final int parameterCount;
    private final int entityViewManagerIndex;
    private final int entityManagerIndex;
    private final int entityIndex;

    public ViewInstanceEntityListener(Method listener) {
        int entityViewManagerIndex = -1;
        int entityManagerIndex = -1;
        int entityIndex = -1;
        Class<?>[] parameterTypes = listener.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (EntityViewManager.class == parameterTypes[i]) {
                entityViewManagerIndex = i;
            } else if (EntityManager.class == parameterTypes[i]) {
                entityManagerIndex = i;
            } else if (entityIndex == -1) {
                entityIndex = i;
            } else {
                throw new IllegalArgumentException("Illegal argument at index " + i + " of type " + parameterTypes[i].getName() + " in lifecycle method: " + listener.getDeclaringClass().getName() + "." + listener.getName() +
                        "! Allowed argument types are [" + EntityViewManager.class.getSimpleName() + ", " + EntityManager.class.getSimpleName() + ", <Entity type or super type>]");
            }
        }
        listener.setAccessible(true);
        this.listener = listener;
        this.parameterCount = parameterTypes.length;
        this.entityViewManagerIndex = entityViewManagerIndex;
        this.entityManagerIndex = entityManagerIndex;
        this.entityIndex = entityIndex;
    }

    protected final void invoke(EntityViewManager entityViewManager, EntityManager entityManager, Object view, Object entity) {
        Object[] parameters = new Object[parameterCount];
        if (entityViewManagerIndex != -1) {
            parameters[entityViewManagerIndex] = entityViewManager;
        }
        if (entityManagerIndex != -1) {
            parameters[entityManagerIndex] = entityManager;
        }
        if (entityIndex != -1) {
            parameters[entityIndex] = entity;
        }
        try {
            listener.invoke(view, parameters);
        } catch (Exception e) {
            throw new RuntimeException("Error happened during invocation of the lifecycle method", e);
        }
    }
}
