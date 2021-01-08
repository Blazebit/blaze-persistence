/*
 * Copyright 2014 - 2021 Blazebit.
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