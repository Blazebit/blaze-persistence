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

package com.blazebit.persistence.impl.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Copied from {@link com.blazebit.persistence.view.impl.metamodel.ClassUtils}.
 * TODO remove once blaze-common-utils contains this functionality
 *
 * @author Christian Beikov
 * @since 1.2
 */
public class ClassUtils {

    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE;
    private static final Map<String, Class<?>> PRIMITIVE_NAME_TO_TYPE;

    static {
        Map<Class<?>, Class<?>> wrapperToPrimitive = new HashMap<Class<?>, Class<?>>();
        wrapperToPrimitive.put(Boolean.class, boolean.class);
        wrapperToPrimitive.put(Byte.class, byte.class);
        wrapperToPrimitive.put(Short.class, short.class);
        wrapperToPrimitive.put(Character.class, char.class);
        wrapperToPrimitive.put(Integer.class, int.class);
        wrapperToPrimitive.put(Long.class, long.class);
        wrapperToPrimitive.put(Float.class, float.class);
        wrapperToPrimitive.put(Double.class, double.class);
        WRAPPER_TO_PRIMITIVE = Collections.unmodifiableMap(wrapperToPrimitive);
        Map<String, Class<?>> primitiveNameToType = new HashMap<String, Class<?>>();
        primitiveNameToType.put(boolean.class.getName(), boolean.class);
        primitiveNameToType.put(byte.class.getName(), byte.class);
        primitiveNameToType.put(short.class.getName(), short.class);
        primitiveNameToType.put(char.class.getName(), char.class);
        primitiveNameToType.put(int.class.getName(), int.class);
        primitiveNameToType.put(long.class.getName(), long.class);
        primitiveNameToType.put(float.class.getName(), float.class);
        primitiveNameToType.put(double.class.getName(), double.class);
        PRIMITIVE_NAME_TO_TYPE = Collections.unmodifiableMap(primitiveNameToType);
    }

    private ClassUtils() {
    }

    public static Class<?> getPrimitiveClassOfWrapper(Class<?> wrapperClass) {
        return WRAPPER_TO_PRIMITIVE.get(wrapperClass);
    }

    public static Class<?> getPrimitiveClassByName(String name) {
        return PRIMITIVE_NAME_TO_TYPE.get(name);
    }
}
