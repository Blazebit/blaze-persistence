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

package com.blazebit.persistence.integration.jsonb;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.config.PropertyVisibilityStrategy;
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
