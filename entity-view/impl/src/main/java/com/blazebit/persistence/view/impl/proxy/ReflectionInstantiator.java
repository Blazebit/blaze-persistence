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

import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.spi.type.BasicDirtyTracker;
import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.TypeConverter;

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
    private final TypeConverterEntry[] typeConverters;

    public ReflectionInstantiator(MappingConstructor<T> mappingConstructor, ProxyFactory proxyFactory, ManagedViewType<T> viewType, ManagedViewTypeImplementor<T> viewTypeBase, Class<?>[] parameterTypes,
                                  List<MutableBasicUserTypeEntry> mutableBasicUserTypes, List<TypeConverterEntry> typeConverterEntries) {
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
        this.typeConverters = typeConverterEntries.toArray(new TypeConverterEntry[typeConverterEntries.size()]);
    }

    public static final class MutableBasicUserTypeEntry {
        private final int index;
        private final BasicUserType<Object> userType;

        public MutableBasicUserTypeEntry(int index, BasicUserType<Object> userType) {
            this.index = index;
            this.userType = userType;
        }
    }

    public static final class TypeConverterEntry {
        private final int index;
        private final TypeConverter<Object, Object> typeConverter;

        public TypeConverterEntry(int index, TypeConverter<Object, Object> typeConverter) {
            this.index = index;
            this.typeConverter = typeConverter;
        }
    }

    @Override
    public T newInstance(Object[] tuple) {
        try {
            // TODO: move this into proxy generated code by setting user types on a volatile static array
            // type conversion
            if (typeConverters.length != 0) {
                for (int i = 0; i < typeConverters.length; i++) {
                    TypeConverterEntry entry = typeConverters[i];
                    tuple[entry.index] = entry.typeConverter.convertToViewType(tuple[entry.index]);
                }
            }
            T instance = constructor.newInstance(tuple);
            if (mutableBasicUserTypes.length != 0) {
                Object[] initialState = ((DirtyStateTrackable) instance).$$_getInitialState();
                for (int i = 0; i < mutableBasicUserTypes.length; i++) {
                    MutableBasicUserTypeEntry entry = mutableBasicUserTypes[i];
                    Object value = initialState[entry.index];
                    if (value != null) {
                        BasicUserType<Object> userType = entry.userType;
                        // User types end up here only if the support dirty checking or if the should be cloned
                        if (value instanceof BasicDirtyTracker) {
                            ((BasicDirtyTracker) value).$$_setParent((BasicDirtyTracker) instance, entry.index);
                        } else {
                            initialState[entry.index] = userType.deepClone(value);
                        }
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
    protected Class<T> getProxyClass(ProxyFactory proxyFactory, ManagedViewType<T> viewType, ManagedViewTypeImplementor<T> viewTypeBase) {
        return (Class<T>) proxyFactory.getProxy(viewType, viewTypeBase);
    }
    
}
