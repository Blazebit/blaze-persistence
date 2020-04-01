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

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AssignmentConstructorReflectionInstantiator<T> extends AbstractReflectionInstantiator<T> {

    private final Constructor<T> constructor;
    private final int size;
    private final int[] assignment;

    public AssignmentConstructorReflectionInstantiator(MappingConstructorImpl<T> mappingConstructor, ProxyFactory proxyFactory, ManagedViewTypeImplementor<T> viewType, Class<?>[] parameterTypes,
                                                       EntityViewManager entityViewManager, ManagedViewTypeImpl.InheritanceSubtypeConfiguration<T> configuration, MappingConstructorImpl.InheritanceSubtypeConstructorConfiguration<T> subtypeConstructorConfiguration) {
        super(configuration.getMutableBasicUserTypes(), configuration.getTypeConverterEntries(), parameterTypes);
        Class<T> proxyClazz = (Class<T>) proxyFactory.getProxy(entityViewManager, viewType);
        Constructor<T> javaConstructor;
        int size;

        try {
            if (mappingConstructor == null) {
                size = 4;
                javaConstructor = proxyClazz.getDeclaredConstructor(proxyClazz, int.class, int[].class, Object[].class);
            } else {
                int parameterSize = subtypeConstructorConfiguration.getOverallPositionAssignment(viewType).length;
                size = parameterSize + 4;
                Class[] types = new Class[size];
                types[0] = proxyClazz;
                types[1] = int.class;
                types[2] = int[].class;
                types[3] = Object[].class;
                System.arraycopy(parameterTypes, parameterTypes.length - parameterSize, types, 4, parameterSize);
                javaConstructor = proxyClazz.getDeclaredConstructor(types);
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
        int[] overallPositionAssignment = configuration.getOverallPositionAssignment(viewType);
        if (mappingConstructor == null) {
            this.assignment = overallPositionAssignment;
        } else {
            int[] overallConstructorPositionAssignment = subtypeConstructorConfiguration.getOverallPositionAssignment(viewType);
            int[] assignment = new int[overallPositionAssignment.length + overallConstructorPositionAssignment.length];
            System.arraycopy(overallPositionAssignment, 0, assignment, 0, overallPositionAssignment.length);
            for (int i = 0; i < overallConstructorPositionAssignment.length; i++) {
                assignment[overallPositionAssignment.length + i] = overallPositionAssignment.length + overallConstructorPositionAssignment[i];
            }
            this.assignment = assignment;
        }
    }

    @Override
    public T newInstance(Object[] tuple) {
        try {
            prepareTuple(tuple);
            Object[] array = new Object[size];
            array[1] = 0;
            array[2] = assignment;
            array[3] = tuple;
            T instance = constructor.newInstance(array);
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
