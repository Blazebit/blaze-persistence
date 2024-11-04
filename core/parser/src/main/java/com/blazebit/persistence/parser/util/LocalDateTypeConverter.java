/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.util;

import java.time.LocalDate;
import java.util.Calendar;

/**
 * @author Christian Beikov
 * @since 1.6.0
 */
public class LocalDateTypeConverter extends TypeUtils.AbstractLiteralFunctionTypeConverter<LocalDate> {

    public static final TypeConverter<?> INSTANCE = new LocalDateTypeConverter();
    private static final long serialVersionUID = 1L;

    private LocalDateTypeConverter() {
        super("literal_local_date");
    }

    public LocalDate convert(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.util.Date) {
            java.util.Date date = (java.util.Date) value;
            return LocalDate.of(date.getYear(), date.getMonth(), date.getDate());
        } else if (value instanceof java.util.Calendar) {
            java.util.Calendar calendar = (java.util.Calendar) value;
            return LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        } else if (value instanceof String) {
            return java.sql.Date.valueOf((String) value).toLocalDate();
        }
        throw unknownConversion(value, LocalDate.class);
    }

    @Override
    public String toString(LocalDate value) {
        return TypeUtils.jdbcDate(
                value.getYear(),
                value.getMonthValue(),
                value.getDayOfMonth()
        );
    }

    @Override
    public void appendTo(LocalDate value, StringBuilder stringBuilder) {
        TypeUtils.appendJdbcDate(
                stringBuilder,
                value.getYear(),
                value.getMonthValue(),
                value.getDayOfMonth()
        );
    }
}