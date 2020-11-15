/*
 * Copyright 2014 - 2020 Blazebit.
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

import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Calendar;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
public class OffsetTimeTypeConverter extends TypeUtils.AbstractLiteralFunctionTypeConverter<OffsetTime> {

    public static final TypeConverter<?> INSTANCE = new OffsetTimeTypeConverter();
    private static final long serialVersionUID = 1L;

    private OffsetTimeTypeConverter() {
        super("literal_offset_time");
    }

    public OffsetTime convert(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.util.Date) {
            java.util.Date date = (java.util.Date) value;
            return OffsetTime.of(date.getHours(), date.getMinutes(), date.getSeconds(), 0, ZoneOffset.ofHours(0));
        } else if (value instanceof Calendar) {
            Calendar calendar = (Calendar) value;
            return OffsetTime.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), 0, ZoneOffset.ofHours(0));
        } else if (value instanceof String) {
            return java.sql.Time.valueOf((String) value).toLocalTime().atOffset(ZoneOffset.ofHours(0));
        }
        throw unknownConversion(value, OffsetTime.class);
    }

    @Override
    public String toString(OffsetTime value) {
        return TypeUtils.jdbcTime(
                value.getHour(),
                value.getMinute(),
                value.getSecond()
        );
    }

    @Override
    public void appendTo(OffsetTime value, StringBuilder stringBuilder) {
        TypeUtils.appendJdbcTime(
                stringBuilder,
                value.getHour(),
                value.getMinute(),
                value.getSecond()
        );
    }
}