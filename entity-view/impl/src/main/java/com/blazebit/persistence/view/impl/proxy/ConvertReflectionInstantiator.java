/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.proxy;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

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
    private final Constructor<T> constructor;
    private final Object[] defaultObject;
    private final AbstractReflectionInstantiator.TypeConverterEntry[] typeConverterEntries;

    public ConvertReflectionInstantiator(ProxyFactory proxyFactory, ManagedViewType<T> viewType, Class<?>[] parameterTypes, int constructorParameterCount, EntityViewManager entityViewManager) {
        @SuppressWarnings("unchecked")
        Class<T> proxyClazz = (Class<T>) proxyFactory.getProxy(entityViewManager, (ManagedViewTypeImplementor<Object>) viewType);
        Constructor<T> javaConstructor;
        Object[] defaultObject;

        try {
            if (TUPLE_STYLE) {
                Class[] types = new Class[constructorParameterCount + 3];
                types[0] = proxyClazz;
                types[1] = int.class;
                types[2] = Object[].class;
                System.arraycopy(parameterTypes, parameterTypes.length - constructorParameterCount, types, 3, constructorParameterCount);
                javaConstructor = proxyClazz.getDeclaredConstructor(types);
                defaultObject = AbstractReflectionInstantiator.createDefaultObject(3, parameterTypes, constructorParameterCount);
                defaultObject[1] = 0;
            } else {
                javaConstructor = proxyClazz.getDeclaredConstructor(parameterTypes);
                defaultObject = null;
            }
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new IllegalArgumentException("Couldn't find expected constructor of the proxy class: " + proxyClazz.getName(), ex);
        }

        this.constructor = javaConstructor;
        this.defaultObject = defaultObject;
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
                Object[] array = Arrays.copyOf(defaultObject, defaultObject.length);
                array[2] = tuple;
                t = constructor.newInstance(array);
            } else {
                t = constructor.newInstance(tuple);
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
