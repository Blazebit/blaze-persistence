package com.blazebit.persistence.view.impl.metamodel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2
 */
public class ClassUtils {

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER;
    private static final Map<String, Class<?>> PRIMITIVE_NAME_TO_TYPE;

    static {
        Map<Class<?>, Class<?>> primitiveToWrapper = new HashMap<Class<?>, Class<?>>();
        primitiveToWrapper.put(boolean.class, Boolean.class);
        primitiveToWrapper.put(byte.class, Byte.class);
        primitiveToWrapper.put(short.class, Short.class);
        primitiveToWrapper.put(char.class, Character.class);
        primitiveToWrapper.put(int.class, Integer.class);
        primitiveToWrapper.put(long.class, Long.class);
        primitiveToWrapper.put(float.class, Float.class);
        primitiveToWrapper.put(double.class, Double.class);
        PRIMITIVE_TO_WRAPPER = Collections.unmodifiableMap(primitiveToWrapper);
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
        return PRIMITIVE_TO_WRAPPER.get(wrapperClass);
    }

    public static Class<?> getPrimitiveClassByName(String name) {
        return PRIMITIVE_NAME_TO_TYPE.get(name);
    }
}
