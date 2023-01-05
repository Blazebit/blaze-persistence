/*
 * Copyright 2014 - 2023 Blazebit.
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;

/**
 * @author Christian Beikov
 * @since 1.6.0
 */
public class LocalDateTimeTypeConverter extends TypeUtils.AbstractLiteralFunctionTypeConverter<LocalDateTime> {

    public static final TypeConverter<?> INSTANCE = new LocalDateTimeTypeConverter();
    private static final long serialVersionUID = 1L;

    private LocalDateTimeTypeConverter() {
        super("literal_local_date_time");
    }

    public LocalDateTime convert(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.util.Date) {
            java.util.Date date = (java.util.Date) value;
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        } else if (value instanceof Calendar) {
            Calendar calendar = (Calendar) value;
            return LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());
        } else if (value instanceof String) {
            return LocalDateTime.ofInstant(java.sql.Timestamp.valueOf((String) value).toInstant(), ZoneId.systemDefault());
        }
        throw unknownConversion(value, LocalDateTime.class);
    }

    @Override
    public String toString(LocalDateTime value) {
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
    public void appendTo(LocalDateTime value, StringBuilder stringBuilder) {
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