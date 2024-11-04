/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Time;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TimeToLocalTimeTypeConverter implements TypeConverter<Time, Object> {

    private static final Method TO_LOCAL_TIME;
    private static final Method VALUE_OF;

    static {
        Method toLocalTime = null;
        Method valueOf = null;
        try {
            Class<?> c = java.sql.Time.class;
            Class<?> localTimeClass = Class.forName("java.time.LocalTime");
            toLocalTime = c.getMethod("toLocalTime");
            valueOf = c.getMethod("valueOf", localTimeClass);
        } catch (Exception e) {
            // Ignore
        }

        TO_LOCAL_TIME = toLocalTime;
        VALUE_OF = valueOf;
    }

    @Override
    public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
        return Time.class;
    }

    @Override
    public Object convertToViewType(Time object) {
        if (object == null) {
            return null;
        }
        try {
            return VALUE_OF.invoke(null, object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Time convertToUnderlyingType(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return (Time) TO_LOCAL_TIME.invoke(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
