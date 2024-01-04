/*
 * Copyright 2014 - 2024 Blazebit.
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