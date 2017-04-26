/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.view.impl.proxy;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImpl;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingConstructor;

/**
 *
 * @author Christian Beikov
 * @since 1.0.6
 */
public class ReflectionInstantiator<T> implements ObjectInstantiator<T> {

    private final Constructor<T> constructor;

    public ReflectionInstantiator(MappingConstructor<T> mappingConstructor, ProxyFactory proxyFactory, ManagedViewType<T> viewType, ManagedViewTypeImpl<T> viewTypeBase, Class<?>[] parameterTypes) {
        Class<T> proxyClazz = getProxyClass(proxyFactory, viewType, viewTypeBase);
        Constructor<T> javaConstructor;

        try {
            javaConstructor = proxyClazz.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new IllegalArgumentException("The given mapping constructor '" + mappingConstructor + "' does not map to a constructor of the proxy class: " + proxyClazz
                .getName(), ex);
        }

        if (javaConstructor == null) {
            throw new IllegalArgumentException("The given mapping constructor '" + mappingConstructor + "' does not map to a constructor of the proxy class: " + proxyClazz
                .getName());
        }

        this.constructor = javaConstructor;
    }

    @Override
    public T newInstance(Object[] tuple) {
        try {
            return constructor.newInstance(tuple);
        } catch (Exception ex) {
            String[] types = new String[tuple.length];
            
            for (int i = 0; i < types.length; i++) {
                if (tuple[i] == null) {
                    types[i] = null;
                } else {
                    types[i] = tuple[i].getClass().getName();
                }
            }
            throw new RuntimeException("Could not invoke the proxy constructor '" + constructor + "' with the given tuple: " + Arrays.toString(tuple) + " with the types: " + Arrays.toString(types), ex);
        }
    }

    @SuppressWarnings("unchecked")
    protected Class<T> getProxyClass(ProxyFactory proxyFactory, ManagedViewType<T> viewType, ManagedViewTypeImpl<T> viewTypeBase) {
        return (Class<T>) proxyFactory.getProxy(viewType, viewTypeBase);
    }
    
}
