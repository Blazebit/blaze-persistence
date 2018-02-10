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

package com.blazebit.persistence.view.impl.proxy;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.metamodel.MappingConstructor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class StaticFactoryReflectionInstantiator<T> extends AbstractReflectionInstantiator<T> {

    private final Method factoryMethod;

    public StaticFactoryReflectionInstantiator(MappingConstructor<T> mappingConstructor, ProxyFactory proxyFactory, ManagedViewTypeImplementor<T> viewType, ManagedViewTypeImplementor<T> viewTypeBase, int inheritanceConfigurationIndex, Class<?>[] parameterTypes,
                                               EntityViewManager entityViewManager, List<MutableBasicUserTypeEntry> mutableBasicUserTypes, List<TypeConverterEntry> typeConverterEntries) {
        super(mutableBasicUserTypes, typeConverterEntries);
        Class<T> proxyClazz = getProxyClass(entityViewManager, proxyFactory, viewType, viewTypeBase);
        Method factoryMethod;

        try {
            if (mappingConstructor == null) {
                factoryMethod = proxyClazz.getDeclaredMethod("create" + inheritanceConfigurationIndex, parameterTypes);
            } else {
                factoryMethod = proxyClazz.getDeclaredMethod("create" + inheritanceConfigurationIndex + "_" + mappingConstructor.getName(), parameterTypes);
            }
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new IllegalArgumentException("The given mapping constructor '" + mappingConstructor + "' does not map to a constructor of the proxy class: " + proxyClazz
                    .getName(), ex);
        }

        if (factoryMethod == null) {
            throw new IllegalArgumentException("The given mapping constructor '" + mappingConstructor + "' does not map to a constructor of the proxy class: " + proxyClazz
                    .getName());
        }

        this.factoryMethod = factoryMethod;
    }

    @Override
    public T newInstance(Object[] tuple) {
        try {
            prepareTuple(tuple);
            @SuppressWarnings("unchecked")
            T instance = (T) factoryMethod.invoke(null, tuple);
            finalizeInstance(instance);
            return instance;
        } catch (Exception ex) {
            String[] types = new String[tuple.length];
            
            for (int i = 0; i < types.length; i++) {
                if (tuple[i] == null) {
                    types[i] = null;
                } else {
                    types[i] = tuple[i].getClass().getName();
                }
            }
            throw new RuntimeException("Could not invoke the static factory proxy method '" + factoryMethod + "' with the given tuple: " + Arrays.toString(tuple) + " with the types: " + Arrays.toString(types), ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Class<T> getProxyClass(EntityViewManager entityViewManager, ProxyFactory proxyFactory, ManagedViewTypeImplementor<T> viewType, ManagedViewTypeImplementor<T> viewTypeBase) {
        return (Class<T>) proxyFactory.getProxy(entityViewManager, viewType, viewTypeBase);
    }
    
}
