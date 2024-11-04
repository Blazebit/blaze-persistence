/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
public class InstantTypeConverter extends TypeUtils.AbstractLiteralFunctionTypeConverter<Instant> {

    public static final TypeConverter<?> INSTANCE = new InstantTypeConverter();
    private static final long serialVersionUID = 1L;

    private InstantTypeConverter() {
        super("literal_instant");
    }

    public Instant convert(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.util.Date) {
            java.util.Date date = (java.util.Date) value;
            Calendar result = Calendar.getInstance();
            result.setTime(date);
            return result.toInstant();
        } else if (value instanceof Calendar) {
            return ((Calendar) value).toInstant();
        } else if (value instanceof String) {
            return java.sql.Timestamp.valueOf((String) value).toInstant();
        }
        throw unknownConversion(value, Instant.class);
    }

    @Override
    public String toString(Instant value) {
        ZonedDateTime zonedDateTime = value.atZone(ZoneId.systemDefault());
        return TypeUtils.jdbcTimestamp(
                zonedDateTime.getYear(),
                zonedDateTime.getMonthValue(),
                zonedDateTime.getDayOfMonth(),
                zonedDateTime.getHour(),
                zonedDateTime.getMinute(),
                zonedDateTime.getSecond(),
                zonedDateTime.getNano()
        );
    }

    @Override
    public void appendTo(Instant value, StringBuilder stringBuilder) {
        ZonedDateTime zonedDateTime = value.atZone(ZoneId.systemDefault());
        TypeUtils.appendJdbcTimestamp(
                stringBuilder,
                zonedDateTime.getYear(),
                zonedDateTime.getMonthValue(),
                zonedDateTime.getDayOfMonth(),
                zonedDateTime.getHour(),
                zonedDateTime.getMinute(),
                zonedDateTime.getSecond(),
                zonedDateTime.getNano()
        );
    }
}