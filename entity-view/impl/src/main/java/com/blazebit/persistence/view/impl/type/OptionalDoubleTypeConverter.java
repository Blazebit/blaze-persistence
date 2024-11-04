/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class OptionalDoubleTypeConverter implements TypeConverter<Object, Object> {

    private static final Method OF;
    private static final Method GET_AS_DOUBLE;
    private static final Method IS_PRESENT;
    private static final Object EMPTY;

    static {
        Method of = null;
        Method getAsDouble = null;
        Method isPresent = null;
        Object empty = null;
        try {
            Class<?> c = Class.forName("java.util.OptionalDouble");
            of = c.getDeclaredMethod("of", double.class);
            getAsDouble = c.getDeclaredMethod("getAsDouble");
            isPresent = c.getMethod("isPresent");
            empty = c.getMethod("empty").invoke(null);
        } catch (Exception e) {
            // Ignore
        }

        OF = of;
        GET_AS_DOUBLE = getAsDouble;
        IS_PRESENT = isPresent;
        EMPTY = empty;
    }

    @Override
    public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
        return Double.class;
    }

    @Override
    public Object convertToViewType(Object object) {
        if (object == null) {
            return EMPTY;
        }
        try {
            return OF.invoke(null, object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object convertToUnderlyingType(Object object) {
        try {
            if (object == null || !((boolean) IS_PRESENT.invoke(object))) {
                return null;
            }
            return GET_AS_DOUBLE.invoke(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
