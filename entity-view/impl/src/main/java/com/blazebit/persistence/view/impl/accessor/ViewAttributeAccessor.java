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

package com.blazebit.persistence.view.impl.accessor;

import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.reflection.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ViewAttributeAccessor implements AttributeAccessor {

    private final Method getter;
    private final Field field;

    ViewAttributeAccessor(EntityViewManagerImpl evm, MethodAttribute<?, ?> attribute, boolean readonly) {
        Class<?> clazz = attribute.getDeclaringType().getJavaType();
        Method getter = ReflectionUtils.getGetter(clazz, attribute.getName());
        if (!Modifier.isPublic(getter.getModifiers()) || !Modifier.isPublic(getter.getDeclaringClass().getModifiers())) {
            try {
                getter.setAccessible(true);
            } catch (Exception e) {
                throw new RuntimeException("Couldn't make method for entity view attribute accessible for reading!", e);
            }
        }
        this.getter = getter;
        if (readonly) {
            this.field = null;
        } else {
            Class<?> proxyClass = evm.getProxyFactory().getProxy(evm, (ManagedViewTypeImplementor<Object>) attribute.getDeclaringType(), null);
            Field f = ReflectionUtils.getField(proxyClass, attribute.getName());
            try {
                f.setAccessible(true);
            } catch (Exception e) {
                throw new RuntimeException("Couldn't make field for entity view attribute accessible for writing!", e);
            }
            this.field = f;
        }
    }

    @Override
    public Object getOrCreateValue(Object object) {
        return getValue(object);
    }

    @Override
    public Object getValue(Object object) {
        try {
            return getter.invoke(object);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't get value from entity view attribute!", e);
        }
    }

    @Override
    public void setValue(Object object, Object value) {
        if (field != null) {
            try {
                field.set(object, value);
            } catch (Exception e) {
                throw new RuntimeException("Couldn't set value of entity view attribute!", e);
            }
        } else {
            throw new RuntimeException("Can't set value with readonly attribute accessor!");
        }
    }
}
