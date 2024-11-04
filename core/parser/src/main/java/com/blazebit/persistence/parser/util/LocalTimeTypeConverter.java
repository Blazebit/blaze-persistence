/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.util;

import java.time.LocalTime;
import java.util.Calendar;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
public class LocalTimeTypeConverter extends TypeUtils.AbstractLiteralFunctionTypeConverter<LocalTime> {

    public static final TypeConverter<?> INSTANCE = new LocalTimeTypeConverter();
    private static final long serialVersionUID = 1L;

    private LocalTimeTypeConverter() {
        super("literal_local_time");
    }

    public LocalTime convert(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.util.Date) {
            java.util.Date date = (java.util.Date) value;
            return LocalTime.of(date.getHours(), date.getMinutes(), date.getSeconds());
        } else if (value instanceof java.util.Calendar) {
            java.util.Calendar calendar = (java.util.Calendar) value;
            return LocalTime.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
        } else if (value instanceof String) {
            return java.sql.Time.valueOf((String) value).toLocalTime();
        }
        throw unknownConversion(value, LocalTime.class);
    }

    @Override
    public String toString(LocalTime value) {
        return TypeUtils.jdbcTime(
                value.getHour(),
                value.getMinute(),
                value.getSecond()
        );
    }

    @Override
    public void appendTo(LocalTime value, StringBuilder stringBuilder) {
        TypeUtils.appendJdbcTime(
                stringBuilder,
                value.getHour(),
                value.getMinute(),
                value.getSecond()
        );
    }
}