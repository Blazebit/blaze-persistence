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

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;
import com.blazebit.persistence.view.spi.type.MutableBasicUserType;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MutableBasicUserTypeRegistry implements BasicUserTypeRegistry {

    private final Map<Class<?>, BasicUserType<?>> basicUserTypes = new HashMap<>();
    private final Map<Class<?>, Map<Class<?>, TypeConverter<?, ?>>> converters = new HashMap<>();

    public MutableBasicUserTypeRegistry() {
        // Immutable types
        basicUserTypes.put(boolean.class, BooleanBasicUserType.INSTANCE);
        basicUserTypes.put(Boolean.class, BooleanBasicUserType.INSTANCE);
        basicUserTypes.put(char.class, CharacterBasicUserType.INSTANCE);
        basicUserTypes.put(Character.class, CharacterBasicUserType.INSTANCE);
        basicUserTypes.put(byte.class, ByteBasicUserType.INSTANCE);
        basicUserTypes.put(Byte.class, ByteBasicUserType.INSTANCE);
        basicUserTypes.put(short.class, ShortBasicUserType.INSTANCE);
        basicUserTypes.put(Short.class, ShortBasicUserType.INSTANCE);
        basicUserTypes.put(int.class, IntegerBasicUserType.INSTANCE);
        basicUserTypes.put(Integer.class, IntegerBasicUserType.INSTANCE);
        basicUserTypes.put(long.class, LongBasicUserType.INSTANCE);
        basicUserTypes.put(Long.class, LongBasicUserType.INSTANCE);
        basicUserTypes.put(float.class, FloatBasicUserType.INSTANCE);
        basicUserTypes.put(Float.class, FloatBasicUserType.INSTANCE);
        basicUserTypes.put(double.class, DoubleBasicUserType.INSTANCE);
        basicUserTypes.put(Double.class, DoubleBasicUserType.INSTANCE);
        basicUserTypes.put(BigInteger.class, BigIntegerBasicUserType.INSTANCE);
        basicUserTypes.put(BigDecimal.class, BigDecimalBasicUserType.INSTANCE);
        basicUserTypes.put(String.class, StringBasicUserType.INSTANCE);

        // Input stream is immutable
        basicUserTypes.put(InputStream.class, ImmutableBasicUserType.INSTANCE);

        // NOTE: Lob types depend on a wrapping converter for handling dirty detection
        basicUserTypes.put(Blob.class, BlobBasicUserType.INSTANCE);
        basicUserTypes.put(Clob.class, ClobBasicUserType.INSTANCE);
        basicUserTypes.put(NClob.class, NClobBasicUserType.INSTANCE);

        basicUserTypes.put(byte[].class, PrimitiveByteArrayBasicUserType.INSTANCE);
        basicUserTypes.put(char[].class, PrimitiveCharArrayBasicUserType.INSTANCE);
        basicUserTypes.put(Byte[].class, ByteArrayBasicUserType.INSTANCE);
        basicUserTypes.put(Character[].class, CharArrayBasicUserType.INSTANCE);

        basicUserTypes.put(java.util.Date.class, DateBasicUserType.INSTANCE);
        basicUserTypes.put(java.sql.Date.class, JavaSqlDateBasicUserType.INSTANCE);
        basicUserTypes.put(java.sql.Time.class, TimeBasicUserType.INSTANCE);
        basicUserTypes.put(java.sql.Timestamp.class, TimestampBasicUserType.INSTANCE);
        basicUserTypes.put(java.util.TimeZone.class, TimeZoneBasicUserType.INSTANCE);

        basicUserTypes.put(java.util.Calendar.class, CalendarBasicUserType.INSTANCE);
        basicUserTypes.put(java.util.GregorianCalendar.class, CalendarBasicUserType.INSTANCE);

        basicUserTypes.put(java.lang.Class.class, ClassBasicUserType.INSTANCE);
        basicUserTypes.put(java.util.Currency.class, CurrencyBasicUserType.INSTANCE);
        basicUserTypes.put(java.util.Locale.class, LocaleBasicUserType.INSTANCE);
        basicUserTypes.put(java.util.UUID.class, UUIDBasicUserType.INSTANCE);
        basicUserTypes.put(java.net.URL.class, URLBasicUserType.INSTANCE);

        // Java 8 time types
        try {
            basicUserTypes.put(Class.forName("java.time.LocalDate"), LocalDateBasicUserType.INSTANCE);
            basicUserTypes.put(Class.forName("java.time.LocalTime"), LocalTimeBasicUserType.INSTANCE);
            basicUserTypes.put(Class.forName("java.time.LocalDateTime"), LocalDateTimeBasicUserType.INSTANCE);
            basicUserTypes.put(Class.forName("java.time.OffsetTime"), OffsetTimeBasicUserType.INSTANCE);
            basicUserTypes.put(Class.forName("java.time.OffsetDateTime"), OffsetDateTimeBasicUserType.INSTANCE);
            basicUserTypes.put(Class.forName("java.time.ZonedDateTime"), ZoneDateTimeBasicUserType.INSTANCE);
            basicUserTypes.put(Class.forName("java.time.Duration"), DurationBasicUserType.INSTANCE);
            basicUserTypes.put(Class.forName("java.time.Instant"), InstantBasicUserType.INSTANCE);
            basicUserTypes.put(Class.forName("java.time.MonthDay"), ImmutableBasicUserType.INSTANCE);
            basicUserTypes.put(Class.forName("java.time.Year"), ImmutableBasicUserType.INSTANCE);
            basicUserTypes.put(Class.forName("java.time.YearMonth"), ImmutableBasicUserType.INSTANCE);
            basicUserTypes.put(Class.forName("java.time.Period"), ImmutableBasicUserType.INSTANCE);
            basicUserTypes.put(Class.forName("java.time.ZoneId"), ZoneIdBasicUserType.INSTANCE);
            basicUserTypes.put(Class.forName("java.time.ZoneOffset"), ZoneOffsetBasicUserType.INSTANCE);
        } catch (ClassNotFoundException ex) {
            // If they aren't found, we ignore them
        }

        // NOTE: keep this in sync with 09_basic_user_type.adoc

        Map<Class<?>, TypeConverter<?, ?>> map = new HashMap<>();
        map.put(Blob.class, BlobTypeConverter.INSTANCE);
        converters.put(Blob.class, map);
        map = new HashMap<>();
        map.put(Clob.class, ClobTypeConverter.INSTANCE);
        converters.put(Clob.class, map);
        map = new HashMap<>();
        map.put(NClob.class, NClobTypeConverter.INSTANCE);
        converters.put(NClob.class, map);
        map = new HashMap<>();
        map.put(Calendar.class, DateToCalendarTypeConverter.JAVA_UTIL_CALENDAR_CONVERTER);
        map.put(GregorianCalendar.class, DateToCalendarTypeConverter.JAVA_UTIL_GREGORIAN_CALENDAR_CONVERTER);
        converters.put(Date.class, map);
        map = new HashMap<>();
        map.put(Date.class, CalendarToDateTypeConverter.JAVA_UTIL_CALENDAR_CONVERTER);
        map.put(java.sql.Date.class, CalendarToDateTypeConverter.JAVA_UTIL_CALENDAR_CONVERTER);
        map.put(Timestamp.class, CalendarToDateTypeConverter.JAVA_UTIL_CALENDAR_CONVERTER);
        converters.put(Calendar.class, map);
        map = new HashMap<>();
        map.put(Date.class, CalendarToDateTypeConverter.JAVA_UTIL_GREGORIAN_CALENDAR_CONVERTER);
        map.put(java.sql.Date.class, CalendarToDateTypeConverter.JAVA_UTIL_GREGORIAN_CALENDAR_CONVERTER);
        map.put(Timestamp.class, CalendarToDateTypeConverter.JAVA_UTIL_GREGORIAN_CALENDAR_CONVERTER);
        converters.put(GregorianCalendar.class, map);

        // Java 8 optional types
        try {
            map = new HashMap<>();
            map.put(Object.class, new OptionalTypeConverter());
            converters.put(Class.forName("java.util.Optional"), map);
            map = new HashMap<>();
            map.put(Integer.class, new OptionalIntTypeConverter());
            converters.put(Class.forName("java.util.OptionalInt"), map);
            map = new HashMap<>();
            map.put(Long.class, new OptionalLongTypeConverter());
            converters.put(Class.forName("java.util.OptionalLong"), map);
            map = new HashMap<>();
            map.put(Double.class, new OptionalDoubleTypeConverter());
            converters.put(Class.forName("java.util.OptionalDouble"), map);
            map = new HashMap<>();
            map.put(java.util.Date.class, DateToLocalDateTypeConverter.JAVA_UTIL_DATE_CONVERTER);
            map.put(java.sql.Date.class, DateToLocalDateTypeConverter.JAVA_SQL_DATE_CONVERTER);
            map.put(java.sql.Timestamp.class, DateToLocalDateTypeConverter.JAVA_SQL_TIMESTAMP_CONVERTER);
            map.put(java.util.Calendar.class, CalendarToLocalDateTypeConverter.JAVA_UTIL_CALENDAR_CONVERTER);
            map.put(java.util.GregorianCalendar.class, CalendarToLocalDateTypeConverter.JAVA_UTIL_GREGORIAN_CALENDAR_CONVERTER);
            converters.put(Class.forName("java.time.LocalDate"), map);
            map = new HashMap<>();
            map.put(java.util.Date.class, DateToLocalDateTimeTypeConverter.JAVA_UTIL_DATE_CONVERTER);
            map.put(java.sql.Timestamp.class, DateToLocalDateTimeTypeConverter.JAVA_SQL_TIMESTAMP_CONVERTER);
            map.put(java.util.Calendar.class, CalendarToLocalDateTimeTypeConverter.JAVA_UTIL_CALENDAR_CONVERTER);
            map.put(java.util.GregorianCalendar.class, CalendarToLocalDateTimeTypeConverter.JAVA_UTIL_GREGORIAN_CALENDAR_CONVERTER);
            converters.put(Class.forName("java.time.LocalDateTime"), map);
            map = new HashMap<>();
            map.put(java.util.Date.class, DateToInstantTypeConverter.JAVA_UTIL_DATE_CONVERTER);
            map.put(java.sql.Timestamp.class, DateToInstantTypeConverter.JAVA_SQL_TIMESTAMP_CONVERTER);
            map.put(java.util.Calendar.class, CalendarToInstantTypeConverter.JAVA_UTIL_CALENDAR_CONVERTER);
            map.put(java.util.GregorianCalendar.class, CalendarToInstantTypeConverter.JAVA_UTIL_GREGORIAN_CALENDAR_CONVERTER);
            converters.put(Class.forName("java.time.Instant"), map);
            map.put(java.sql.Time.class, new TimeToLocalTimeTypeConverter());
            converters.put(Class.forName("java.time.LocalTime"), map);
        } catch (ClassNotFoundException ex) {
            // If they aren't found, we ignore them
        }
    }

    @Override
    public <X> void registerBasicUserType(Class<X> clazz, BasicUserType<X> userType) {
        basicUserTypes.put(clazz, userType);
    }

    @Override
    public <X, Y> void registerTypeConverter(Class<X> entityModelType, Class<Y> viewModelType, TypeConverter<X, Y> converter) {
        Map<Class<?>, TypeConverter<?, ?>> converterMap = converters.get(viewModelType);
        if (converterMap == null) {
            converterMap = new HashMap<>();
            converters.put(viewModelType, converterMap);
        }
        converterMap.put(entityModelType, converter);
    }

    @Override
    public Map<Class<?>, Map<Class<?>, TypeConverter<?, ?>>> getTypeConverters() {
        return converters;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y> Map<Class<?>, TypeConverter<?, Y>> getTypeConverter(Class<Y> clazz) {
        Map<Class<?>, TypeConverter<?, Y>> map = (Map<Class<?>, TypeConverter<?, Y>>) (Map<?, ?>) converters.get(clazz);
        if (map == null) {
            return Collections.emptyMap();
        }
        return map;
    }

    @Override
    public Map<Class<?>, BasicUserType<?>> getBasicUserTypes() {
        return basicUserTypes;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X> BasicUserType<X> getBasicUserType(Class<X> clazz) {
        BasicUserType<?> userType = basicUserTypes.get(clazz);
        if (userType == null) {
            if (clazz.isEnum()) {
                //noinspection rawtypes
                userType = new EnumBasicUserType<>((Class<Enum>) clazz);
            } else if (java.sql.Date.class == clazz) {
                userType = JavaSqlDateBasicUserType.INSTANCE;
            } else if ( Time.class == clazz) {
                userType = TimeBasicUserType.INSTANCE;
            } else if (Timestamp.class == clazz) {
                userType = TimestampBasicUserType.INSTANCE;
            } else if (Date.class.isAssignableFrom(clazz)) {
                userType = DateBasicUserType.INSTANCE;
            } else if (Calendar.class.isAssignableFrom(clazz)) {
                userType = CalendarBasicUserType.INSTANCE;
            } else {
                userType = MutableBasicUserType.INSTANCE;
            }
        }

        return (BasicUserType<X>) userType;
    }
}
