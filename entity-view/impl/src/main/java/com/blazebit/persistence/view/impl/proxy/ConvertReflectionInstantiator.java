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
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.spi.type.DirtyStateTrackable;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ConvertReflectionInstantiator<T> implements ObjectInstantiator<T> {

    private static final boolean TUPLE_STYLE = true;
    private final boolean resetInitialState;
    private final Constructor<T> constructor;
    private final int size;
    private final AbstractReflectionInstantiator.TypeConverterEntry[] typeConverterEntries;

    public ConvertReflectionInstantiator(ProxyFactory proxyFactory, ManagedViewType<T> viewType, Class<?>[] parameterTypes, int constructorParameterCount, boolean resetInitialState, EntityViewManager entityViewManager) {
        @SuppressWarnings("unchecked")
        Class<T> proxyClazz = (Class<T>) proxyFactory.getProxy(entityViewManager, (ManagedViewTypeImplementor<Object>) viewType);
        Constructor<T> javaConstructor;
        int size;

        try {
            if (TUPLE_STYLE) {
                size = constructorParameterCount + 3;
                Class[] types = new Class[constructorParameterCount + 3];
                types[0] = proxyClazz;
                types[1] = int.class;
                types[2] = Object[].class;
                System.arraycopy(parameterTypes, parameterTypes.length - constructorParameterCount, types, 3, constructorParameterCount);
                javaConstructor = proxyClazz.getDeclaredConstructor(types);
            } else {
                size = parameterTypes.length;
                javaConstructor = proxyClazz.getDeclaredConstructor(parameterTypes);
            }
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new IllegalArgumentException("Couldn't find expected constructor of the proxy class: " + proxyClazz.getName(), ex);
        }

        this.resetInitialState = resetInitialState && DirtyStateTrackable.class.isAssignableFrom(proxyClazz);
        this.constructor = javaConstructor;
        this.size = size;
        this.typeConverterEntries = AbstractReflectionInstantiator.withPrimitiveConverters(Collections.<AbstractReflectionInstantiator.TypeConverterEntry>emptyList(), parameterTypes);
    }

    @Override
    public T newInstance(Object[] tuple) {
        try {
            // TODO: move this into proxy generated code by setting user types on a static AtomicReferenceArray
            // type conversion
            for (int i = 0; i < typeConverterEntries.length; i++) {
                AbstractReflectionInstantiator.TypeConverterEntry entry = typeConverterEntries[i];
                tuple[entry.index] = entry.typeConverter.convertToViewType(tuple[entry.index]);
            }
            T t;
            if (TUPLE_STYLE) {
                Object[] array = new Object[size];
                array[1] = 0;
                array[2] = tuple;
                t = constructor.newInstance(array);
            } else {
                t = constructor.newInstance(tuple);
            }
            if (resetInitialState) {
                Object[] initialState = ((DirtyStateTrackable) t).$$_getInitialState();
                for (int i = 0; i < initialState.length; i++) {
                    initialState[i] = null;
                }
            }
            return t;
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
