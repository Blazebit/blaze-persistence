/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class CalendarToLocalDateTypeConverter<T extends Calendar> extends AbstractLocalDateTypeConverter<T> {

    private static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");

    public static final CalendarToLocalDateTypeConverter<Calendar> JAVA_UTIL_CALENDAR_CONVERTER = new CalendarToLocalDateTypeConverter<Calendar>() {

        @Override
        public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
            return Calendar.class;
        }

        @Override
        public Calendar convertToUnderlyingType(Object object) {
            if (object == null) {
                return null;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(UTC_TIMEZONE);
            calendar.setTimeInMillis(toEpochDay(object) * MILLISECOND_CONVERSION_FACTOR);
            return calendar;
        }

    };

    public static final CalendarToLocalDateTypeConverter<GregorianCalendar> JAVA_UTIL_GREGORIAN_CALENDAR_CONVERTER = new CalendarToLocalDateTypeConverter<GregorianCalendar>() {

        @Override
        public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
            return GregorianCalendar.class;
        }

        @Override
        public GregorianCalendar convertToUnderlyingType(Object object) {
            if (object == null) {
                return null;
            }
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTimeZone(UTC_TIMEZONE);
            gregorianCalendar.setTimeInMillis(toEpochDay(object) * MILLISECOND_CONVERSION_FACTOR);
            return gregorianCalendar;
        }

    };

    @Override
    public Object convertToViewType(Calendar object) {
        if (object == null) {
            return null;
        }
        return ofEpochDay(object.getTimeInMillis() / MILLISECOND_CONVERSION_FACTOR);
    }
}
