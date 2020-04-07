/*
 * Copyright 2014 - 2020 Blazebit.
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
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.MappingConstructorImpl;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0.6
 */
public class ConstructorReflectionInstantiator<T> extends AbstractReflectionInstantiator<T> {

    private static final boolean TUPLE_STYLE = true;
    private final Constructor<T> constructor;
    private final int size;

    public ConstructorReflectionInstantiator(MappingConstructorImpl<T> mappingConstructor, ProxyFactory proxyFactory, ManagedViewTypeImplementor<T> viewType, Class<?>[] parameterTypes,
                                             EntityViewManager entityViewManager, ManagedViewTypeImpl.InheritanceSubtypeConfiguration<T> configuration) {
        this(mappingConstructor, proxyFactory, viewType, parameterTypes, entityViewManager, configuration.getMutableBasicUserTypes(), configuration.getTypeConverterEntries());
    }

    public ConstructorReflectionInstantiator(MappingConstructorImpl<T> mappingConstructor, ProxyFactory proxyFactory, ManagedViewTypeImplementor<T> viewType, Class<?>[] parameterTypes,
                                             EntityViewManager entityViewManager, List<MutableBasicUserTypeEntry> mutableBasicUserTypes, List<TypeConverterEntry> typeConverterEntries) {
        super(mutableBasicUserTypes, typeConverterEntries, parameterTypes);
        Class<T> proxyClazz = (Class<T>) proxyFactory.getProxy(entityViewManager, viewType);
        Constructor<T> javaConstructor;
        int size;

        try {
            if (TUPLE_STYLE) {
                if (mappingConstructor == null) {
                    size = 3;
                    javaConstructor = proxyClazz.getDeclaredConstructor(proxyClazz, int.class, Object[].class);
                } else {
                    int parameterSize = mappingConstructor.getParameterAttributes().size();
                    size = parameterSize + 3;
                    Class[] types = new Class[size];
                    types[0] = proxyClazz;
                    types[1] = int.class;
                    types[2] = Object[].class;
                    System.arraycopy(parameterTypes, parameterTypes.length - parameterSize, types, 3, parameterSize);
                    javaConstructor = proxyClazz.getDeclaredConstructor(types);
                }
            } else {
                javaConstructor = proxyClazz.getDeclaredConstructor(parameterTypes);

            }
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new IllegalArgumentException("The given mapping constructor '" + mappingConstructor + "' does not map to a constructor of the proxy class: " + proxyClazz
                    .getName(), ex);
        }

        if (javaConstructor == null) {
            throw new IllegalArgumentException("The given mapping constructor '" + mappingConstructor + "' does not map to a constructor of the proxy class: " + proxyClazz
                    .getName());
        }

        this.constructor = javaConstructor;
        this.size = size;
    }

    @Override
    public T newInstance(Object[] tuple) {
        try {
            prepareTuple(tuple);
            T instance;
            if (TUPLE_STYLE) {
                Object[] array = new Object[size];
                array[1] = 0;
                array[2] = tuple;
                instance = constructor.newInstance(array);
            } else {
                instance = constructor.newInstance(tuple);
            }
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
            throw new RuntimeException("Could not invoke the proxy constructor '" + constructor + "' with the given tuple: " + Arrays.toString(tuple) + " with the types: " + Arrays.toString(types), ex);
        }
    }

}
