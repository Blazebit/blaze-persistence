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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TypeUtils {

    public static final TypeConverter<Enum<?>> ENUM_CONVERTER = new AbstractTypeConverter<Enum<?>>() {

        private static final long serialVersionUID = 1L;

        @Override
        public Enum<?> convert(Object value) {
            throw unknownConversion(value, Enum.class);
        }

        @Override
        public String toString(Enum<?> value) {
            StringBuilder sb = new StringBuilder(value.getDeclaringClass().getName().length() + value.name().length() + 1);
            appendTo(value, sb);
            return sb.toString();
        }

        @Override
        public void appendTo(Enum<?> value, StringBuilder sb) {
            sb.append(value.getDeclaringClass().getName());
            sb.append('.');
            sb.append(value.name());
        }
    };

    public static final TypeConverter<Character> CHARACTER_CONVERTER = new AbstractTypeConverter<Character>() {

        private static final long serialVersionUID = 1L;

        @Override
        public Character convert(Object value) {
            return value == null ? null : value.toString().charAt(0);
        }

        @Override
        public String toString(Character value) {
            StringBuilder sb = new StringBuilder(3);
            appendTo(value, sb);
            return sb.toString();
        }

        @Override
        public void appendTo(Character value, StringBuilder sb) {
            sb.append('\'');
            char c = value;
            if (c == '\'') {
                sb.append('\'');
                sb.append('\'');
            } else {
                sb.append(c);
            }
            sb.append('\'');
        }
    };

    public static final TypeConverter<String> STRING_CONVERTER = new AbstractTypeConverter<String>() {

        private static final long serialVersionUID = 1L;

        @Override
        public String convert(Object value) {
            return value == null ? null : value.toString();
        }

        @Override
        public String toString(String value) {
            StringBuilder sb = new StringBuilder(value.length() + 20);
            appendTo(value, sb);
            return sb.toString();
        }

        @Override
        public void appendTo(String value, StringBuilder sb) {
            sb.append('\'');
            for (int i = 0; i < value.length(); i++) {
                final char c = value.charAt(i);
                if (c == '\'') {
                    sb.append('\'');
                    sb.append('\'');
                } else {
                    sb.append(c);
                }
            }
            sb.append('\'');
        }
    };

    public static final TypeConverter<Boolean> BOOLEAN_CONVERTER = new AbstractTypeConverter<Boolean>() {

        private static final long serialVersionUID = 1L;

        public Boolean convert(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof Boolean) {
                return (Boolean) value;
            } else if (value instanceof String) {
                return Boolean.valueOf((String) value);
            }
            throw unknownConversion(value, Boolean.class);
        }

        @Override
        public void appendTo(Boolean value, StringBuilder stringBuilder) {
            stringBuilder.append(value.booleanValue());
        }
    };

    public static final TypeConverter<Byte> BYTE_CONVERTER = new AbstractTypeConverter<Byte>() {

        private static final long serialVersionUID = 1L;

        public Byte convert(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof Number) {
                return ((Number) value).byteValue();
            } else if (value instanceof String) {
                return Byte.valueOf((String) value);
            }
            throw unknownConversion(value, Byte.class);
        }

        @Override
        public void appendTo(Byte value, StringBuilder stringBuilder) {
            stringBuilder.append(value.byteValue());
        }
    };

    public static final TypeConverter<Short> SHORT_CONVERTER = new AbstractTypeConverter<Short>() {

        private static final long serialVersionUID = 1L;

        public Short convert(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof Number) {
                return ((Number) value).shortValue();
            } else if (value instanceof String) {
                return Short.valueOf((String) value);
            }
            throw unknownConversion(value, Short.class);
        }

        @Override
        public void appendTo(Short value, StringBuilder stringBuilder) {
            stringBuilder.append(value.shortValue());
        }
    };

    public static final TypeConverter<Integer> INTEGER_CONVERTER = new AbstractTypeConverter<Integer>() {

        private static final long serialVersionUID = 1L;

        public Integer convert(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof Number) {
                return ((Number) value).intValue();
            } else if (value instanceof String) {
                return Integer.valueOf((String) value);
            }
            throw unknownConversion(value, Integer.class);
        }

        @Override
        public void appendTo(Integer value, StringBuilder stringBuilder) {
            stringBuilder.append(value.intValue());
        }
    };

    public static final TypeConverter<Long> LONG_CONVERTER = new AbstractTypeConverter<Long>() {

        private static final long serialVersionUID = 1L;

        public Long convert(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof Number) {
                return ((Number) value).longValue();
            } else if (value instanceof String) {
                return Long.valueOf((String) value);
            }
            throw unknownConversion(value, Long.class);
        }

        @Override
        public void appendTo(Long value, StringBuilder sb) {
            sb.append(value.longValue()).append('L');
        }
    };

    public static final TypeConverter<Float> FLOAT_CONVERTER = new AbstractTypeConverter<Float>() {

        private static final long serialVersionUID = 1L;

        public Float convert(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            } else if (value instanceof String) {
                return Float.valueOf((String) value);
            }
            throw unknownConversion(value, Float.class);
        }

        @Override
        public void appendTo(Float value, StringBuilder sb) {
            sb.append(value.floatValue()).append('F');
        }
    };

    public static final TypeConverter<Double> DOUBLE_CONVERTER = new AbstractTypeConverter<Double>() {

        private static final long serialVersionUID = 1L;

        public Double convert(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof String) {
                return Double.valueOf((String) value);
            }
            throw unknownConversion(value, Double.class);
        }

        @Override
        public void appendTo(Double value, StringBuilder sb) {
            sb.append(value.doubleValue()).append('D');
        }
    };

    public static final TypeConverter<BigInteger> BIG_INTEGER_CONVERTER = new AbstractTypeConverter<BigInteger>() {

        private static final long serialVersionUID = 1L;

        public BigInteger convert(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof Number) {
                return BigInteger.valueOf(((Number) value).longValue());
            } else if (value instanceof String) {
                return new BigInteger((String) value);
            }
            throw unknownConversion(value, BigInteger.class);
        }

        @Override
        public void appendTo(BigInteger value, StringBuilder sb) {
            sb.append(value).append("BI");
        }
    };

    public static final TypeConverter<BigDecimal> BIG_DECIMAL_CONVERTER = new AbstractTypeConverter<BigDecimal>() {

        private static final long serialVersionUID = 1L;

        public BigDecimal convert(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof BigInteger) {
                return new BigDecimal((BigInteger) value);
            } else if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            } else if (value instanceof String) {
                return new BigDecimal((String) value);
            }
            throw unknownConversion(value, BigDecimal.class);
        }

        @Override
        public void appendTo(BigDecimal value, StringBuilder sb) {
            sb.append(value).append("BD");
        }
    };

    public static final TypeConverter<java.sql.Time> TIME_CONVERTER = new AbstractLiteralFunctionTypeConverter<java.sql.Time>("literal_time") {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings({ "deprecation" })
        public java.sql.Time convert(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof java.util.Date) {
                java.util.Date date = (java.util.Date) value;
                java.sql.Time result = new java.sql.Time(date.getHours(), date.getMinutes(), date.getSeconds());
                return result;
            } else if (value instanceof java.util.Calendar) {
                java.util.Calendar calendar = (java.util.Calendar) value;
                java.sql.Time result = new java.sql.Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
                return result;
            } else if (value instanceof String) {
                return java.sql.Time.valueOf((String) value);
            }
            throw unknownConversion(value, java.sql.Time.class);
        }

        @Override
        @SuppressWarnings("deprecation")
        public String toString(Time value) {
            return jdbcTime(value.getHours(), value.getMinutes(), value.getSeconds());
        }

        @Override
        @SuppressWarnings("deprecation")
        public void appendTo(Time value, StringBuilder stringBuilder) {
            appendJdbcTime(stringBuilder, value.getHours(), value.getMinutes(), value.getSeconds());
        }
    };

    public static final TypeConverter<java.util.Date> DATE_AS_TIME_CONVERTER = new AbstractLiteralFunctionTypeConverter<java.util.Date>("literal_util_date") {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings({ "deprecation" })
        public java.util.Date convert(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof java.util.Date) {
                java.util.Date date = (java.util.Date) value;
                java.sql.Time result = new java.sql.Time(date.getHours(), date.getMinutes(), date.getSeconds());
                return result;
            } else if (value instanceof java.util.Calendar) {
                java.util.Calendar calendar = (java.util.Calendar) value;
                java.sql.Time result = new java.sql.Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
                return result;
            } else if (value instanceof String) {
                return java.sql.Time.valueOf((String) value);
            }
            throw unknownConversion(value, java.util.Date.class);
        }

        @Override
        @SuppressWarnings("deprecation")
        public String toString(Date value) {
            return jdbcTime(value.getHours(), value.getMinutes(), value.getSeconds());
        }

        @Override
        @SuppressWarnings("deprecation")
        public void appendTo(Date value, StringBuilder stringBuilder) {
            appendJdbcTime(stringBuilder, value.getHours(), value.getMinutes(), value.getSeconds());
        }
    };

    public static final TypeConverter<java.sql.Date> DATE_CONVERTER = new AbstractLiteralFunctionTypeConverter<java.sql.Date>("literal_date") {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings({ "deprecation" })
        public java.sql.Date convert(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof java.util.Date) {
                java.util.Date date = (java.util.Date) value;
                java.sql.Date result = new java.sql.Date(date.getYear(), date.getMonth(), date.getDate());
                return result;
            } else if (value instanceof java.util.Calendar) {
                java.util.Calendar calendar = (java.util.Calendar) value;
                java.sql.Date result = new java.sql.Date(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
                return result;
            } else if (value instanceof String) {
                return java.sql.Date.valueOf((String) value);
            }
            throw unknownConversion(value, java.sql.Date.class);
        }

        @Override
        @SuppressWarnings("deprecation")
        public String toString(java.sql.Date value) {
            return jdbcDate(value.getYear() + 1900, value.getMonth() + 1, value.getDate());
        }

        @Override
        @SuppressWarnings("deprecation")
        public void appendTo(java.sql.Date value, StringBuilder stringBuilder) {
            appendJdbcDate(stringBuilder, value.getYear() + 1900, value.getMonth() + 1, value.getDate());
        }
    };

    public static final TypeConverter<java.util.Date> DATE_AS_DATE_CONVERTER = new AbstractLiteralFunctionTypeConverter<java.util.Date>("literal_date") {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings({ "deprecation" })
        public java.sql.Date convert(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof java.util.Date) {
                java.util.Date date = (java.util.Date) value;
                java.sql.Date result = new java.sql.Date(date.getYear(), date.getMonth(), date.getDate());
                return result;
            } else if (value instanceof java.util.Calendar) {
                java.util.Calendar calendar = (java.util.Calendar) value;
                java.sql.Date result = new java.sql.Date(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
                return result;
            } else if (value instanceof String) {
                return java.sql.Date.valueOf((String) value);
            }
            throw unknownConversion(value, java.sql.Date.class);
        }

        @Override
        @SuppressWarnings("deprecation")
        public String toString(Date value) {
            return jdbcDate(value.getYear() + 1900, value.getMonth() + 1, value.getDate());
        }

        @Override
        @SuppressWarnings("deprecation")
        public void appendTo(Date value, StringBuilder stringBuilder) {
            appendJdbcDate(stringBuilder, value.getYear() + 1900, value.getMonth() + 1, value.getDate());
        }
    };

    public static final TypeConverter<java.sql.Timestamp> TIMESTAMP_CONVERTER = new AbstractLiteralFunctionTypeConverter<java.sql.Timestamp>("literal_timestamp") {

        private static final long serialVersionUID = 1L;

        public java.sql.Timestamp convert(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof java.util.Date) {
                java.util.Date date = (java.util.Date) value;
                java.sql.Timestamp result = new java.sql.Timestamp(date.getTime());
                return result;
            } else if (value instanceof java.util.Calendar) {
                java.util.Calendar calendar = (java.util.Calendar) value;
                java.sql.Timestamp result = new java.sql.Timestamp(calendar.getTimeInMillis());
                return result;
            } else if (value instanceof String) {
                return java.sql.Timestamp.valueOf((String) value);
            }
            throw unknownConversion(value, java.sql.Timestamp.class);
        }

        @Override
        @SuppressWarnings("deprecation")
        public String toString(Timestamp value) {
            return jdbcTimestamp(
                    value.getYear() + 1900,
                    value.getMonth() + 1,
                    value.getDate(),
                    value.getHours(),
                    value.getMinutes(),
                    value.getSeconds(),
                    value.getNanos()
            );
        }

        @Override
        @SuppressWarnings("deprecation")
        public void appendTo(Timestamp value, StringBuilder stringBuilder) {
            appendJdbcTimestamp(
                    stringBuilder,
                    value.getYear() + 1900,
                    value.getMonth() + 1,
                    value.getDate(),
                    value.getHours(),
                    value.getMinutes(),
                    value.getSeconds(),
                    value.getNanos()
            );
        }
    };

    public static final TypeConverter<java.util.Date> DATE_TIMESTAMP_CONVERTER = new AbstractLiteralFunctionTypeConverter<java.util.Date>("literal_util_date") {

        private static final long serialVersionUID = 1L;

        public java.util.Date convert(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof java.util.Date) {
                java.util.Date date = (java.util.Date) value;
                java.sql.Timestamp result = new java.sql.Timestamp(date.getTime());
                return result;
            } else if (value instanceof java.util.Calendar) {
                java.util.Calendar calendar = (java.util.Calendar) value;
                java.sql.Timestamp result = new java.sql.Timestamp(calendar.getTimeInMillis());
                return result;
            } else if (value instanceof String) {
                return java.sql.Timestamp.valueOf((String) value);
            }
            throw unknownConversion(value, java.util.Date.class);
        }

        @Override
        @SuppressWarnings("deprecation")
        public String toString(Date value) {
            return jdbcTimestamp(
                value.getYear() + 1900,
                value.getMonth() + 1,
                value.getDate(),
                value.getHours(),
                value.getMinutes(),
                value.getSeconds(),
                (int) ((value.getTime() % 1000) * 1000)
            );
        }

        @Override
        @SuppressWarnings("deprecation")
        public void appendTo(Date value, StringBuilder stringBuilder) {
            appendJdbcTimestamp(
                    stringBuilder,
                    value.getYear() + 1900,
                    value.getMonth() + 1,
                    value.getDate(),
                    value.getHours(),
                    value.getMinutes(),
                    value.getSeconds(),
                    (int) ((value.getTime() % 1000) * 1000)
            );
        }
    };

    public static final TypeConverter<java.util.Calendar> CALENDAR_CONVERTER = new AbstractLiteralFunctionTypeConverter<java.util.Calendar>("literal_calendar") {

        private static final long serialVersionUID = 1L;

        public java.util.Calendar convert(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof java.util.Date) {
                java.util.Date date = (java.util.Date) value;
                Calendar result = Calendar.getInstance();
                result.setTime(date);
                return result;
            } else if (value instanceof java.util.Calendar) {
                return (java.util.Calendar) value;
            } else if (value instanceof String) {
                Calendar result = Calendar.getInstance();
                result.setTime(java.sql.Timestamp.valueOf((String) value));
                return result;
            }
            throw unknownConversion(value, java.util.Calendar.class);
        }

        @Override
        public String toString(Calendar value) {
            return jdbcTimestamp(
                value.get(Calendar.YEAR),
                value.get(Calendar.MONTH) + 1,
                value.get(Calendar.DATE),
                value.get(Calendar.HOUR_OF_DAY),
                value.get(Calendar.MINUTE),
                value.get(Calendar.SECOND),
                value.get(Calendar.MILLISECOND) * 1_000_000
            );
        }

        @Override
        public void appendTo(Calendar value, StringBuilder stringBuilder) {
            appendJdbcTimestamp(
                    stringBuilder,
                    value.get(Calendar.YEAR),
                    value.get(Calendar.MONTH) + 1,
                    value.get(Calendar.DATE),
                    value.get(Calendar.HOUR_OF_DAY),
                    value.get(Calendar.MINUTE),
                    value.get(Calendar.SECOND),
                    value.get(Calendar.MILLISECOND) * 1_000_000
            );
        }
    };

    private static final Logger LOG = Logger.getLogger(TypeUtils.class.getName());
    private static final Map<Class<?>, TypeConverter<?>> CONVERTERS;
    private static final Set<TypeConverter<?>> TEMPORAL_CONVERTERS;

    /**
     * Abstract type converter.
     *
     * @param <T> The converted type
     * @author Christian Beikov
     * @since 1.2.0
     */
    abstract static class AbstractTypeConverter<T> implements TypeConverter<T>, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public String toString(T value) {
            StringBuilder sb = new StringBuilder();
            appendTo(value, sb);
            return sb.toString();
        }

        protected static IllegalArgumentException unknownConversion(Object value, Class<?> targetType) {
            String type = value == null ? "unknown" : value.getClass().getName();
            return new IllegalArgumentException("Could not convert '" + value + "' of type '" + type + "' to the type '" + targetType.getName() + "'!");
        }
    }

    /**
     * Abstract type converter.
     *
     * @param <T> The converted type
     * @author Christian Beikov
     * @since 1.6.0
     */
    abstract static class AbstractLiteralFunctionTypeConverter<T> extends AbstractTypeConverter<T> implements LiteralFunctionTypeConverter<T> {

        private static final long serialVersionUID = 1L;

        private final String literalFunctionName;

        public AbstractLiteralFunctionTypeConverter(String literalFunctionName) {
            this.literalFunctionName = literalFunctionName;
        }

        @Override
        public String getLiteralFunctionName() {
            return literalFunctionName;
        }
    }

    private TypeUtils() {
    }

    static String jdbcTimestamp(int year, int month, int date, int hour, int minute, int second, int nanos) {
        StringBuilder sb = new StringBuilder(36);
        appendJdbcTimestamp(sb, year, month, date, hour, minute, second, nanos);
        return sb.toString();
    }

    static void appendJdbcTimestamp(StringBuilder sb, int year, int month, int date, int hour, int minute, int second, int nanos) {
        sb.append("{ts '");
        
        sb.append(year);
        sb.append('-');
        
        if (month < 10) {
            sb.append('0');
        }
        
        sb.append(month);
        sb.append('-');
        
        if (date < 10) {
            sb.append('0');
        }
        
        sb.append(date);
        sb.append(' ');
        
        if (hour < 10) {
            sb.append('0');
        }
        
        sb.append(hour);
        sb.append(':');
        
        if (minute < 10) {
            sb.append('0');
        }
        
        sb.append(minute);
        sb.append(':');
        
        if (second < 10) {
            sb.append('0');
        }
        
        sb.append(second);
        
        if (nanos > 0) {
            sb.append('.');
            String nanoString = Integer.toString(nanos);
            for (int zeros = 9 - nanoString.length(); zeros > 0; zeros--) {
                sb.append('0');
            }
            sb.append(nanoString);
        }
        
        sb.append("'}");
    }

    static String jdbcDate(int year, int month, int date) {
        StringBuilder sb = new StringBuilder(16);
        appendJdbcDate(sb, year, month, date);
        return sb.toString();
    }

    static void appendJdbcDate(StringBuilder sb, int year, int month, int date) {
        sb.append("{d '");

        sb.append(year);
        sb.append('-');

        if (month < 10) {
            sb.append('0');
        }

        sb.append(month);
        sb.append('-');

        if (date < 10) {
            sb.append('0');
        }

        sb.append(date);

        sb.append("'}");
    }

    static String jdbcTime(int hour, int minute, int second) {
        StringBuilder sb = new StringBuilder(14);
        appendJdbcTime(sb, hour, minute, second);
        return sb.toString();
    }

    static void appendJdbcTime(StringBuilder sb, int hour, int minute, int second) {
        sb.append("{t '");

        if (hour < 10) {
            sb.append('0');
        }

        sb.append(hour);
        sb.append(':');

        if (minute < 10) {
            sb.append('0');
        }

        sb.append(minute);
        sb.append(':');

        if (second < 10) {
            sb.append('0');
        }

        sb.append(second);

        sb.append("'}");
    }

    static {
        Map<Class<?>, TypeConverter<?>> c = new HashMap<Class<?>, TypeConverter<?>>();
        c.put(String.class, STRING_CONVERTER);
        c.put(Boolean.class, BOOLEAN_CONVERTER);
        c.put(Boolean.TYPE, BOOLEAN_CONVERTER);
        c.put(Byte.class, BYTE_CONVERTER);
        c.put(Byte.TYPE, BYTE_CONVERTER);
        c.put(Short.class, SHORT_CONVERTER);
        c.put(Short.TYPE, SHORT_CONVERTER);
        c.put(Character.class, CHARACTER_CONVERTER);
        c.put(Character.TYPE, CHARACTER_CONVERTER);
        c.put(Integer.class, INTEGER_CONVERTER);
        c.put(Integer.TYPE, INTEGER_CONVERTER);
        c.put(Long.class, LONG_CONVERTER);
        c.put(Long.TYPE, LONG_CONVERTER);
        c.put(Float.class, FLOAT_CONVERTER);
        c.put(Float.TYPE, FLOAT_CONVERTER);
        c.put(Double.class, DOUBLE_CONVERTER);
        c.put(Double.TYPE, DOUBLE_CONVERTER);
        c.put(BigInteger.class, BIG_INTEGER_CONVERTER);
        c.put(BigDecimal.class, BIG_DECIMAL_CONVERTER);
        c.put(java.sql.Time.class, TIME_CONVERTER);
        c.put(java.sql.Date.class, DATE_CONVERTER);
        c.put(java.sql.Timestamp.class, TIMESTAMP_CONVERTER);
        c.put(java.util.Date.class, DATE_TIMESTAMP_CONVERTER);
        c.put(java.util.Calendar.class, CALENDAR_CONVERTER);
        Set<TypeConverter<?>> temporalConverters = new HashSet<>();
        temporalConverters.add(TIME_CONVERTER);
        temporalConverters.add(DATE_CONVERTER);
        temporalConverters.add(TIMESTAMP_CONVERTER);
        temporalConverters.add(DATE_TIMESTAMP_CONVERTER);
        temporalConverters.add(CALENDAR_CONVERTER);

        try {
            c.put(Class.forName("java.time.LocalDate"), LocalDateTypeConverter.INSTANCE);
            temporalConverters.add(LocalDateTypeConverter.INSTANCE);
            c.put(Class.forName("java.time.LocalTime"), LocalTimeTypeConverter.INSTANCE);
            temporalConverters.add(LocalTimeTypeConverter.INSTANCE);
            c.put(Class.forName("java.time.LocalDateTime"), LocalDateTimeTypeConverter.INSTANCE);
            temporalConverters.add(LocalDateTimeTypeConverter.INSTANCE);
            c.put(Class.forName("java.time.Instant"), InstantTypeConverter.INSTANCE);
            temporalConverters.add(InstantTypeConverter.INSTANCE);
            c.put(Class.forName("java.time.ZonedDateTime"), ZonedDateTimeTypeConverter.INSTANCE);
            temporalConverters.add(ZonedDateTimeTypeConverter.INSTANCE);
            c.put(Class.forName("java.time.OffsetTime"), OffsetTimeTypeConverter.INSTANCE);
            temporalConverters.add(OffsetTimeTypeConverter.INSTANCE);
            c.put(Class.forName("java.time.OffsetDateTime"), OffsetDateTimeTypeConverter.INSTANCE);
            temporalConverters.add(OffsetDateTimeTypeConverter.INSTANCE);
        } catch (Exception ex) {
            // Ignore
        }
        Set<TypeConverterContributor> contributors = new TreeSet<>(new TypeConverterContributorComparator());
        for (Iterator<TypeConverterContributor> iterator = ServiceLoader.load(TypeConverterContributor.class).iterator(); iterator.hasNext(); ) {
            try {
                TypeConverterContributor typeConverterContributor = iterator.next();
                contributors.add(typeConverterContributor);
            } catch (Throwable t) {
                LOG.log(Level.WARNING, "Couldn't load contributor", t);
            }
        }

        for (TypeConverterContributor contributor : contributors) {
            try {
                contributor.registerTypeConverters(c);
            } catch (Throwable t) {
                LOG.log(Level.SEVERE, "An error occurred while trying to register type converters through: " + contributor.getClass().getName(), t);
            }
        }

        // Copy the map since it could have "escaped" through the register calls and we don't want converter changes at runtime
        if (!contributors.isEmpty()) {
            c = new HashMap<>(c);
        }
        CONVERTERS = Collections.unmodifiableMap(c);
        TEMPORAL_CONVERTERS = temporalConverters;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static String asLiteral(Object value, Set<String> supportedEnumTypes) {
        TypeConverter<Object> converter = (TypeConverter<Object>) getConverter(value.getClass(), supportedEnumTypes);
        if (converter == null) {
            return null;
        }
        return converter.toString(value);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> TypeConverter<T> getConverter(Class<T> targetType, Set<String> supportedEnumTypes) {
        TypeConverter t = CONVERTERS.get(targetType);
        
        if (t == null) {
            if (targetType.isEnum()) {
                if (supportedEnumTypes == null || supportedEnumTypes.contains(targetType.getName())) {
                    t = TypeUtils.ENUM_CONVERTER;
                }
            } else if (java.sql.Time.class.isAssignableFrom(targetType)) {
                t = TypeUtils.TIME_CONVERTER;
            } else if (java.sql.Date.class.isAssignableFrom(targetType)) {
                t = TypeUtils.DATE_CONVERTER;
            } else if (java.sql.Timestamp.class.isAssignableFrom(targetType)) {
                t = TypeUtils.TIMESTAMP_CONVERTER;
            } else if (java.util.Date.class.isAssignableFrom(targetType)) {
                t = TypeUtils.DATE_TIMESTAMP_CONVERTER;
            } else if (java.util.Calendar.class.isAssignableFrom(targetType)) {
                t = TypeUtils.CALENDAR_CONVERTER;
            }
        }
        
        return (TypeConverter<T>) t;
    }

    public static boolean isCharacter(Object value) {
        return value instanceof String || value instanceof Character;
    }

    public static boolean isNumeric(Class<?> type) {
        return Number.class.isAssignableFrom(type) || type == Byte.TYPE || type == Short.TYPE || type == Integer.TYPE || type == Long.TYPE
            || type == Float.TYPE || type == Double.TYPE;
    }

    public static boolean isNumeric(Object value) {
        return Number.class.isInstance(value);
    }

    public static boolean isBoolean(Object value) {
        return Boolean.class.isInstance(value);
    }

    public static boolean isTemporalConverter(TypeConverter<?> c) {
        return TEMPORAL_CONVERTERS.contains(c);
    }

    @SuppressWarnings({ "unchecked" })
    public static <T> T convert(Object value, Class<T> targetType, Set<String> supportedEnumTypes) {
        if (value == null) {
            return null;
        }
        if (targetType.equals(value.getClass())) {
            return (T) value;
        }

        TypeConverter<T> valueHandler = getConverter(targetType, supportedEnumTypes);
        if (valueHandler == null) {
            throw unknownConversion(value, targetType);
        }

        return valueHandler.convert(value);
    }

    private static IllegalArgumentException unknownConversion(Object value, Class<?> targetType) {
        String type = value == null ? "unknown" : value.getClass().getName();
        return new IllegalArgumentException("Could not convert '" + value + "' of type '" + type + "' to the type '" + targetType.getName() + "'!");
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.6.0
     */
    private static final class TypeConverterContributorComparator implements Comparator<TypeConverterContributor> {
        @Override
        public int compare(TypeConverterContributor o1, TypeConverterContributor o2) {
            int cmp = Integer.compare(o1.priority(), o2.priority());
            if (cmp == 0) {
                cmp = o1.getClass().getName().compareTo(o2.getClass().getName());
            }
            return cmp;
        }
    }
}
