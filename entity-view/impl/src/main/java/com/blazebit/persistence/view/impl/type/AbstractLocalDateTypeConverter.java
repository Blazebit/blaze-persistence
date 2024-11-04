/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.lang.reflect.Method;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractLocalDateTypeConverter<T> implements TypeConverter<T, Object> {

    protected static final long MILLISECOND_CONVERSION_FACTOR = 24 * 60 * 60 * 1000;
    private static final Method OF_EPOCH_DAY;
    private static final Method TO_EPOCH_DAY;

    static {
        Method ofEpochDay = null;
        Method toEpochDay = null;
        try {
            Class<?> c = Class.forName("java.time.LocalDate");
            ofEpochDay = c.getDeclaredMethod("ofEpochDay", long.class);
            toEpochDay = c.getDeclaredMethod("toEpochDay");
        } catch (Exception e) {
            // Ignore
        }

        OF_EPOCH_DAY = ofEpochDay;
        TO_EPOCH_DAY = toEpochDay;
    }

    protected final Object ofEpochDay(long epochDay) {
        try {
            return OF_EPOCH_DAY.invoke(null, epochDay);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected final long toEpochDay(Object object) {
        try {
            return (long) TO_EPOCH_DAY.invoke(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
