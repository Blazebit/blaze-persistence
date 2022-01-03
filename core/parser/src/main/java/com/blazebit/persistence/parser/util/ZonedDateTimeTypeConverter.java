/*
 * Copyright 2014 - 2022 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.parser.util;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;

/**
 * @author Christian Beikov
 * @since 1.6.0
 */
public class ZonedDateTimeTypeConverter extends TypeUtils.AbstractLiteralFunctionTypeConverter<ZonedDateTime> {

    public static final TypeConverter<?> INSTANCE = new ZonedDateTimeTypeConverter();
    private static final long serialVersionUID = 1L;

    private ZonedDateTimeTypeConverter() {
        super("literal_zoned_date_time");
    }

    public ZonedDateTime convert(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.util.Date) {
            java.util.Date date = (java.util.Date) value;
            return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        } else if (value instanceof Calendar) {
            Calendar calendar = (Calendar) value;
            return ZonedDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());
        } else if (value instanceof String) {
            return ZonedDateTime.ofInstant(java.sql.Timestamp.valueOf((String) value).toInstant(), ZoneId.systemDefault());
        }
        throw unknownConversion(value, ZonedDateTime.class);
    }

    @Override
    public String toString(ZonedDateTime value) {
        value = value.withZoneSameInstant(ZoneOffset.UTC);
        return TypeUtils.jdbcTimestamp(
                value.getYear(),
                value.getMonthValue(),
                value.getDayOfMonth(),
                value.getHour(),
                value.getMinute(),
                value.getSecond(),
                value.getNano()
        );
    }

    @Override
    public void appendTo(ZonedDateTime value, StringBuilder stringBuilder) {
        value = value.withZoneSameInstant(ZoneOffset.UTC);
        TypeUtils.appendJdbcTimestamp(
                stringBuilder,
                value.getYear(),
                value.getMonthValue(),
                value.getDayOfMonth(),
                value.getHour(),
                value.getMinute(),
                value.getSecond(),
                value.getNano()
        );
    }
}