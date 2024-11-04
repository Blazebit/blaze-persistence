/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;

/**
 * @author Christian Beikov
 * @since 1.6.0
 */
public class OffsetDateTimeTypeConverter extends TypeUtils.AbstractLiteralFunctionTypeConverter<OffsetDateTime> {

    public static final TypeConverter<?> INSTANCE = new OffsetDateTimeTypeConverter();
    private static final long serialVersionUID = 1L;

    private OffsetDateTimeTypeConverter() {
        super("literal_offset_date_time");
    }

    public OffsetDateTime convert(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.util.Date) {
            java.util.Date date = (java.util.Date) value;
            return OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        } else if (value instanceof Calendar) {
            Calendar calendar = (Calendar) value;
            return OffsetDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());
        } else if (value instanceof String) {
            return OffsetDateTime.ofInstant(java.sql.Timestamp.valueOf((String) value).toInstant(), ZoneId.systemDefault());
        }
        throw unknownConversion(value, LocalDateTime.class);
    }

    @Override
    public String toString(OffsetDateTime value) {
        ZonedDateTime zonedDateTime = value.atZoneSameInstant(ZoneId.systemDefault());
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
    public void appendTo(OffsetDateTime value, StringBuilder stringBuilder) {
        ZonedDateTime zonedDateTime = value.atZoneSameInstant(ZoneId.systemDefault());
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