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
 * @since 1.4.0
 */
public abstract class AbstractInstantTypeConverter<T> implements TypeConverter<T, Object> {

    private static final Method OF_EPOCH_MILLI;
    private static final Method TO_EPOCH_MILLI;

    static {
        Method ofEpochMilli = null;
        Method toEpochMilli = null;
        try {
            Class<?> instantClass = Class.forName("java.time.Instant");
            ofEpochMilli = instantClass.getMethod("ofEpochMilli", long.class);
            toEpochMilli = instantClass.getMethod("toEpochMilli");
        } catch (Exception e) {
            // Ignore
        }

        OF_EPOCH_MILLI = ofEpochMilli;
        TO_EPOCH_MILLI = toEpochMilli;
    }

    protected final Object ofEpochMilli(long milli) {
        try {
            return OF_EPOCH_MILLI.invoke(null, milli);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected final long toEpochMillis(Object object) {
        try {
            return (long) TO_EPOCH_MILLI.invoke(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
