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
 * @since 1.4.0
 */
public abstract class DateToInstantTypeConverter<T extends Date> extends AbstractInstantTypeConverter<T> {


    public static final DateToInstantTypeConverter<Date> JAVA_UTIL_DATE_CONVERTER = new DateToInstantTypeConverter<Date>() {

        @Override
        public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
            return Date.class;
        }

        @Override
        public Date convertToUnderlyingType(Object object) {
            if (object == null) {
                return null;
            }
            return new Date(toEpochMillis(object));
        }

    };

    public static final DateToInstantTypeConverter<Date> JAVA_SQL_TIMESTAMP_CONVERTER = new DateToInstantTypeConverter<Date>() {

        @Override
        public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
            return Timestamp.class;
        }

        @Override
        public Date convertToUnderlyingType(Object object) {
            if (object == null) {
                return null;
            }
            return new Timestamp(toEpochMillis(object));
        }

    };

    @Override
    public Object convertToViewType(Date object) {
        if (object == null) {
            return null;
        }
        return ofEpochMilli(object.getTime());
    }
}
