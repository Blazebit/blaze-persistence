/*
 * Copyright 2014 - 2024 Blazebit.
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

import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.MappingConstructorImpl;
import com.blazebit.persistence.view.impl.type.PrimitiveBooleanTypeConverter;
import com.blazebit.persistence.view.impl.type.PrimitiveByteTypeConverter;
import com.blazebit.persistence.view.impl.type.PrimitiveCharTypeConverter;
import com.blazebit.persistence.view.impl.type.PrimitiveDoubleTypeConverter;
import com.blazebit.persistence.view.impl.type.PrimitiveFloatTypeConverter;
import com.blazebit.persistence.view.impl.type.PrimitiveIntTypeConverter;
import com.blazebit.persistence.view.impl.type.PrimitiveLongTypeConverter;
import com.blazebit.persistence.view.impl.type.PrimitiveShortTypeConverter;
import com.blazebit.persistence.view.spi.type.BasicDirtyTracker;
import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.DirtyStateTrackable;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractReflectionInstantiator<T> implements ObjectInstantiator<T> {

    private static final Map<Class<?>, TypeConverter<Object, Object>> PRIMITIVE_TYPE_CONVERTERS;
    private static final Map<Class<?>, Object> DEFAULT_VALUES;

    static {
        Map<Class<?>, TypeConverter<Object, Object>> primitiveTypeConverters = new HashMap<>();
        primitiveTypeConverters.put(boolean.class, PrimitiveBooleanTypeConverter.INSTANCE);
        primitiveTypeConverters.put(byte.class, PrimitiveByteTypeConverter.INSTANCE);
        primitiveTypeConverters.put(char.class, PrimitiveCharTypeConverter.INSTANCE);
        primitiveTypeConverters.put(short.class, PrimitiveShortTypeConverter.INSTANCE);
        primitiveTypeConverters.put(int.class, PrimitiveIntTypeConverter.INSTANCE);
        primitiveTypeConverters.put(long.class, PrimitiveLongTypeConverter.INSTANCE);
        primitiveTypeConverters.put(float.class, PrimitiveFloatTypeConverter.INSTANCE);
        primitiveTypeConverters.put(double.class, PrimitiveDoubleTypeConverter.INSTANCE);
        PRIMITIVE_TYPE_CONVERTERS = primitiveTypeConverters;
        Map<Class<?>, Object> defaultValues = new HashMap<>();
        defaultValues.put(boolean.class, false);
        defaultValues.put(byte.class, (byte) 0);
        defaultValues.put(char.class, (char) 0);
        defaultValues.put(short.class, (short) 0);
        defaultValues.put(int.class, 0);
        defaultValues.put(long.class, 0L);
        defaultValues.put(float.class, 0F);
        defaultValues.put(double.class, 0D);
        DEFAULT_VALUES = defaultValues;
    }

    protected final MutableBasicUserTypeEntry[] mutableBasicUserTypes;
    protected final TypeConverterEntry[] typeConverters;

    public AbstractReflectionInstantiator(List<MutableBasicUserTypeEntry> mutableBasicUserTypes, List<TypeConverterEntry> typeConverterEntries, Class<?>[] parameterTypes) {
        this.mutableBasicUserTypes = mutableBasicUserTypes.toArray(new MutableBasicUserTypeEntry[mutableBasicUserTypes.size()]);
        this.typeConverters = withPrimitiveConverters(typeConverterEntries, parameterTypes);
    }

    public static Object[] createDefaultObject(int offset, Class<?>[] parameterTypes, int constructorParameterCount) {
        Object[] array = new Object[offset + constructorParameterCount];
        int parameterOffset = parameterTypes.length - constructorParameterCount;
        for (int i = 0; i < constructorParameterCount; i++) {
            array[offset + i] = DEFAULT_VALUES.get(parameterTypes[parameterOffset + i]);
        }
        return array;
    }

    public static <T> ObjectInstantiator<T> createInstantiator(MappingConstructorImpl<T> mappingConstructor, ProxyFactory proxyFactory, ManagedViewTypeImplementor<T> managedViewType, Class<?>[] constructorParameterTypes, EntityViewManagerImpl evm, List<MutableBasicUserTypeEntry> mutableBasicUserTypes, List<TypeConverterEntry> typeConverterEntries) {
        if (managedViewType.getJavaType().isInterface() || Modifier.isAbstract(managedViewType.getJavaType().getModifiers())) {
            return new TupleConstructorReflectionInstantiator<>(mappingConstructor, proxyFactory, managedViewType, constructorParameterTypes, evm, mutableBasicUserTypes, typeConverterEntries);
        } else {
            return new DirectConstructorReflectionInstantiator<>(mappingConstructor, proxyFactory, managedViewType, constructorParameterTypes, evm, mutableBasicUserTypes, typeConverterEntries);
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static final class MutableBasicUserTypeEntry {
        final int index;
        final BasicUserType<Object> userType;

        public MutableBasicUserTypeEntry(int index, BasicUserType<Object> userType) {
            this.index = index;
            this.userType = userType;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static final class TypeConverterEntry {
        final int index;
        final TypeConverter<Object, Object> typeConverter;

        public TypeConverterEntry(int index, TypeConverter<Object, Object> typeConverter) {
            this.index = index;
            this.typeConverter = typeConverter;
        }
    }

    protected final void prepareTuple(Object[] tuple) {
        // TODO: move this into proxy generated code by setting user types on a static AtomicReferenceArray
        // type conversion
        for (int i = 0; i < typeConverters.length; i++) {
            TypeConverterEntry entry = typeConverters[i];
            tuple[entry.index] = entry.typeConverter.convertToViewType(tuple[entry.index]);
        }
    }

    protected final void finalizeInstance(Object instance) {
        if (mutableBasicUserTypes.length != 0) {
            Object[] initialState = ((DirtyStateTrackable) instance).$$_getInitialState();
            for (int i = 0; i < mutableBasicUserTypes.length; i++) {
                MutableBasicUserTypeEntry entry = mutableBasicUserTypes[i];
                Object value = initialState[entry.index];
                if (value != null) {
                    BasicUserType<Object> userType = entry.userType;
                    // User types end up here only if they support dirty checking or if they should be cloned
                    if (userType.supportsDirtyTracking() && value instanceof BasicDirtyTracker) {
                        ((BasicDirtyTracker) value).$$_setParent((BasicDirtyTracker) instance, entry.index);
                    } else {
                        initialState[entry.index] = userType.deepClone(value);
                    }
                }
            }
        }
    }

    static TypeConverterEntry[] withPrimitiveConverters(List<TypeConverterEntry> typeConverterEntries, Class<?>[] parameterTypes) {
        List<TypeConverterEntry> primitiveConverters = new ArrayList<>(parameterTypes.length);
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (parameterType.isPrimitive()) {
                primitiveConverters.add(new TypeConverterEntry(i, PRIMITIVE_TYPE_CONVERTERS.get(parameterType)));
            }
        }

        TypeConverterEntry[] array = new TypeConverterEntry[typeConverterEntries.size() + primitiveConverters.size()];
        int index = 0;
        for (TypeConverterEntry typeConverterEntry : typeConverterEntries) {
            array[index++] = typeConverterEntry;
        }
        for (TypeConverterEntry typeConverterEntry : primitiveConverters) {
            array[index++] = typeConverterEntry;
        }
        return array;
    }

    
}
