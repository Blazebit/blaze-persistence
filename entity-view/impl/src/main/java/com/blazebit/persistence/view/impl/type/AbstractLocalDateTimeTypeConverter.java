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
public abstract class AbstractLocalDateTimeTypeConverter<T> implements TypeConverter<T, Object> {

    private static final Method OF_INSTANT;
    private static final Method OF_EPOCH_MILLI;
    private static final Method TO_INSTANT;
    private static final Method TO_EPOCH_MILLI;
    private static final Object ZONE_ID;
    private static final Object ZONE_OFFSET;

    static {
        Method ofInstant = null;
        Method ofEpochMilli = null;
        Method toInstant = null;
        Method toEpochMilli = null;
        Object zoneId = null;
        Object zoneOffset = null;
        try {
            Class<?> c = Class.forName("java.time.LocalDateTime");
            Class<?> instantClass = Class.forName("java.time.Instant");
            Class<?> zoneIdClass = Class.forName("java.time.ZoneId");
            Class<?> zoneOffsetClass = Class.forName("java.time.ZoneOffset");
            ofInstant = c.getMethod("ofInstant", instantClass, zoneIdClass);
            toInstant = c.getMethod("toInstant", zoneOffsetClass);
            ofEpochMilli = instantClass.getMethod("ofEpochMilli", long.class);
            toEpochMilli = instantClass.getMethod("toEpochMilli");
            zoneId = zoneIdClass.getMethod("systemDefault").invoke(null);
            zoneOffset = zoneOffsetClass.getMethod("systemDefault").invoke(null);
        } catch (Exception e) {
            // Ignore
        }

        OF_INSTANT = ofInstant;
        OF_EPOCH_MILLI = ofEpochMilli;
        TO_INSTANT = toInstant;
        TO_EPOCH_MILLI = toEpochMilli;
        ZONE_ID = zoneId;
        ZONE_OFFSET = zoneOffset;
    }

    protected final Object ofEpochMilli(long milli) {
        try {
            Object instant = OF_EPOCH_MILLI.invoke(null, milli);
            return OF_INSTANT.invoke(null, instant, ZONE_ID);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected final long toEpochMillis(Object object) {
        try {
            Object instant = TO_INSTANT.invoke(object, ZONE_OFFSET);
            return (long) TO_EPOCH_MILLI.invoke(instant);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
