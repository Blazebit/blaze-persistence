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

import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImpl;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.spi.BasicUserType;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0.6
 */
public class ReflectionInstantiator<T> implements ObjectInstantiator<T> {

    private final Constructor<T> constructor;
    private final MutableBasicUserTypeEntry[] mutableBasicUserTypes;

    public ReflectionInstantiator(MappingConstructor<T> mappingConstructor, ProxyFactory proxyFactory, ManagedViewType<T> viewType, ManagedViewTypeImpl<T> viewTypeBase, Class<?>[] parameterTypes, List<MutableBasicUserTypeEntry> mutableBasicUserTypes) {
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
        this.mutableBasicUserTypes = mutableBasicUserTypes.toArray(new MutableBasicUserTypeEntry[mutableBasicUserTypes.size()]);
    }

    public static final class MutableBasicUserTypeEntry {
        private final int index;
        private final BasicUserType<Object> userType;

        public MutableBasicUserTypeEntry(int index, BasicUserType<Object> userType) {
            this.index = index;
            this.userType = userType;
        }
    }

    @Override
    public T newInstance(Object[] tuple) {
        try {
            T instance = constructor.newInstance(tuple);
            if (mutableBasicUserTypes.length != 0) {
                Object[] initialState = ((DirtyStateTrackable) instance).$$_getInitialState();
                for (int i = 0; i < mutableBasicUserTypes.length; i++) {
                    MutableBasicUserTypeEntry entry = mutableBasicUserTypes[i];
                    Object value = initialState[entry.index];
                    if (value != null) {
                        initialState[entry.index] = entry.userType.deepClone(value);
                    }
                }
            }
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

    @SuppressWarnings("unchecked")
    protected Class<T> getProxyClass(ProxyFactory proxyFactory, ManagedViewType<T> viewType, ManagedViewTypeImpl<T> viewTypeBase) {
        return (Class<T>) proxyFactory.getProxy(viewType, viewTypeBase);
    }
    
}
