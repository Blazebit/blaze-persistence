/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public abstract class CalendarToDateTypeConverter<T extends Calendar> implements TypeConverter<T, Date> {

    public static final CalendarToDateTypeConverter<Calendar> JAVA_UTIL_CALENDAR_CONVERTER = new CalendarToDateTypeConverter<Calendar>() {

        @Override
        public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
            return Calendar.class;
        }

        @Override
        public Calendar convertToUnderlyingType(Date object) {
            if (object == null) {
                return null;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(object);
            return calendar;
        }

    };

    public static final CalendarToDateTypeConverter<GregorianCalendar> JAVA_UTIL_GREGORIAN_CALENDAR_CONVERTER = new CalendarToDateTypeConverter<GregorianCalendar>() {

        @Override
        public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
            return GregorianCalendar.class;
        }

        @Override
        public GregorianCalendar convertToUnderlyingType(Date object) {
            if (object == null) {
                return null;
            }
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTime(object);
            return gregorianCalendar;
        }

    };

    @Override
    public Date convertToViewType(Calendar object) {
        if (object == null) {
            return null;
        }
        return object.getTime();
    }
}
