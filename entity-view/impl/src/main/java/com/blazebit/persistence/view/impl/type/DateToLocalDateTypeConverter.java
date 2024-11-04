/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class DateToLocalDateTypeConverter<T extends Date> extends AbstractLocalDateTypeConverter<T> {

    public static final DateToLocalDateTypeConverter<Date> JAVA_UTIL_DATE_CONVERTER = new DateToLocalDateTypeConverter<Date>() {

        @Override
        public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
            return Date.class;
        }

        @Override
        public Date convertToUnderlyingType(Object object) {
            if (object == null) {
                return null;
            }
            return new Date(toEpochDay(object) * MILLISECOND_CONVERSION_FACTOR);
        }

    };

    public static final DateToLocalDateTypeConverter<Date> JAVA_SQL_DATE_CONVERTER = new DateToLocalDateTypeConverter<Date>() {

        @Override
        public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
            return java.sql.Date.class;
        }

        @Override
        public Date convertToUnderlyingType(Object object) {
            if (object == null) {
                return null;
            }
            return new java.sql.Date(toEpochDay(object) * MILLISECOND_CONVERSION_FACTOR);
        }

    };

    public static final DateToLocalDateTypeConverter<Date> JAVA_SQL_TIMESTAMP_CONVERTER = new DateToLocalDateTypeConverter<Date>() {

        @Override
        public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
            return Timestamp.class;
        }

        @Override
        public Date convertToUnderlyingType(Object object) {
            if (object == null) {
                return null;
            }
            return new Timestamp(toEpochDay(object) * MILLISECOND_CONVERSION_FACTOR);
        }

    };

    @Override
    public Object convertToViewType(Date object) {
        if (object == null) {
            return null;
        }
        return ofEpochDay(object.getTime() / MILLISECOND_CONVERSION_FACTOR);
    }
}
