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
public abstract class CalendarToLocalDateTimeTypeConverter<T extends Calendar> extends AbstractLocalDateTimeTypeConverter<T> {

    private static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");

    public static final CalendarToLocalDateTimeTypeConverter<Calendar> JAVA_UTIL_CALENDAR_CONVERTER = new CalendarToLocalDateTimeTypeConverter<Calendar>() {

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
            calendar.setTimeInMillis(toEpochMillis(object));
            return calendar;
        }

    };

    public static final CalendarToLocalDateTimeTypeConverter<GregorianCalendar> JAVA_UTIL_GREGORIAN_CALENDAR_CONVERTER = new CalendarToLocalDateTimeTypeConverter<GregorianCalendar>() {

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
            gregorianCalendar.setTimeInMillis(toEpochMillis(object));
            return gregorianCalendar;
        }

    };

    @Override
    public Object convertToViewType(Calendar object) {
        if (object == null) {
            return null;
        }
        return ofEpochMilli(object.getTimeInMillis());
    }
}
