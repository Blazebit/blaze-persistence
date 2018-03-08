/*
 * Copyright 2014 - 2018 Blazebit.
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TypeUtils {

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

    public static final TypeConverter<java.sql.Time> TIME_CONVERTER = new AbstractTypeConverter<java.sql.Time>() {

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
        public String toString(java.sql.Time value) {
            return jdbcTime(value.getHours(), value.getMinutes(), value.getSeconds());
        }

        @Override
        @SuppressWarnings("deprecation")
        public void appendTo(Time value, StringBuilder stringBuilder) {
            appendJdbcTime(stringBuilder, value.getHours(), value.getMinutes(), value.getSeconds());
        }
    };

    public static final TypeConverter<java.util.Date> DATE_AS_TIME_CONVERTER = new AbstractTypeConverter<java.util.Date>() {

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
            throw unknownConversion(value, java.sql.Time.class);
        }

        @Override
        @SuppressWarnings("deprecation")
        public String toString(java.util.Date value) {
            return jdbcTime(value.getHours(), value.getMinutes(), value.getSeconds());
        }

        @Override
        @SuppressWarnings("deprecation")
        public void appendTo(Date value, StringBuilder stringBuilder) {
            appendJdbcTime(stringBuilder, value.getHours(), value.getMinutes(), value.getSeconds());
        }
    };

    public static final TypeConverter<java.sql.Date> DATE_CONVERTER = new AbstractTypeConverter<java.sql.Date>() {

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

    public static final TypeConverter<java.util.Date> DATE_AS_DATE_CONVERTER = new AbstractTypeConverter<java.util.Date>() {

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
        public String toString(java.util.Date value) {
            return jdbcDate(value.getYear() + 1900, value.getMonth() + 1, value.getDate());
        }

        @Override
        @SuppressWarnings("deprecation")
        public void appendTo(java.util.Date value, StringBuilder stringBuilder) {
            appendJdbcDate(stringBuilder, value.getYear() + 1900, value.getMonth() + 1, value.getDate());
        }
    };

    public static final TypeConverter<java.sql.Timestamp> TIMESTAMP_CONVERTER = new AbstractTypeConverter<java.sql.Timestamp>() {

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
        public String toString(java.sql.Timestamp value) {
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
        public void appendTo(java.sql.Timestamp value, StringBuilder stringBuilder) {
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

    public static final TypeConverter<java.util.Date> DATE_TIMESTAMP_CONVERTER = new AbstractTypeConverter<java.util.Date>() {

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
            throw unknownConversion(value, java.sql.Timestamp.class);
        }

        @Override
        @SuppressWarnings("deprecation")
        public String toString(java.util.Date value) {
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

    public static final TypeConverter<java.util.Calendar> CALENDAR_CONVERTER = new AbstractTypeConverter<java.util.Calendar>() {

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
        public String toString(java.util.Calendar value) {
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

    private static final Map<Class<?>, TypeConverter<?>> CONVERTERS;

    /**
     * Abstract type converter.
     *
     * @param <T> The converted type
     * @author Christian Beikov
     * @since 1.2.0
     */
    private abstract static class AbstractTypeConverter<T> implements TypeConverter<T>, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public String toString(T value) {
            StringBuilder sb = new StringBuilder();
            appendTo(value, sb);
            return sb.toString();
        }
    }

    private TypeUtils() {
    }

    private static String jdbcTimestamp(int year, int month, int date, int hour, int minute, int second, int nanos) {
        StringBuilder sb = new StringBuilder(36);
        appendJdbcTimestamp(sb, year, month, date, hour, minute, second, nanos);
        return sb.toString();
    }

    private static void appendJdbcTimestamp(StringBuilder sb, int year, int month, int date, int hour, int minute, int second, int nanos) {
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

    private static String jdbcDate(int year, int month, int date) {
        StringBuilder sb = new StringBuilder(16);
        appendJdbcDate(sb, year, month, date);
        return sb.toString();
    }

    private static void appendJdbcDate(StringBuilder sb, int year, int month, int date) {
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

    private static String jdbcTime(int hour, int minute, int second) {
        StringBuilder sb = new StringBuilder(14);
        appendJdbcTime(sb, hour, minute, second);
        return sb.toString();
    }

    private static void appendJdbcTime(StringBuilder sb, int hour, int minute, int second) {
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
        CONVERTERS = Collections.unmodifiableMap(c);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> TypeConverter<T> getConverter(Class<T> targetType) {
        TypeConverter t = CONVERTERS.get(targetType);
        
        if (t == null) {
            if (java.sql.Time.class.isAssignableFrom(targetType)) {
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

    @SuppressWarnings({ "unchecked" })
    public static <T> T convert(Object value, Class<T> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.equals(value.getClass())) {
            return (T) value;
        }

        TypeConverter<T> valueHandler = getConverter(targetType);
        if (valueHandler == null) {
            throw unknownConversion(value, targetType);
        }

        return valueHandler.convert(value);
    }

    private static IllegalArgumentException unknownConversion(Object value, Class<?> targetType) {
        String type = value == null ? "unknown" : value.getClass().getName();
        return new IllegalArgumentException("Could not convert '" + value + "' of type '" + type + "' to the type '" + targetType.getName() + "'!");
    }
}
