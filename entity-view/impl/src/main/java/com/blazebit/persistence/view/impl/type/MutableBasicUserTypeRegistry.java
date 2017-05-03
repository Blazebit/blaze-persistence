/*
 * Copyright 2014 - 2017 Blazebit.
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

import com.blazebit.persistence.view.spi.BasicUserType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MutableBasicUserTypeRegistry implements BasicUserTypeRegistry {

    private final Map<Class<?>, BasicUserType<?>> basicUserTypes = new HashMap<>();

    public MutableBasicUserTypeRegistry() {
        // Immutable types
        basicUserTypes.put(boolean.class, ImmutableBasicUserType.INSTANCE);
        basicUserTypes.put(Boolean.class, ImmutableBasicUserType.INSTANCE);
        basicUserTypes.put(char.class, ImmutableBasicUserType.INSTANCE);
        basicUserTypes.put(Character.class, ImmutableBasicUserType.INSTANCE);
        basicUserTypes.put(byte.class, ImmutableBasicUserType.INSTANCE);
        basicUserTypes.put(Byte.class, ImmutableBasicUserType.INSTANCE);
        basicUserTypes.put(short.class, ShortBasicUserType.INSTANCE);
        basicUserTypes.put(Short.class, ShortBasicUserType.INSTANCE);
        basicUserTypes.put(int.class, IntegerBasicUserType.INSTANCE);
        basicUserTypes.put(Integer.class, IntegerBasicUserType.INSTANCE);
        basicUserTypes.put(long.class, LongBasicUserType.INSTANCE);
        basicUserTypes.put(Long.class, LongBasicUserType.INSTANCE);
        basicUserTypes.put(float.class, ImmutableBasicUserType.INSTANCE);
        basicUserTypes.put(Float.class, ImmutableBasicUserType.INSTANCE);
        basicUserTypes.put(double.class, ImmutableBasicUserType.INSTANCE);
        basicUserTypes.put(Double.class, ImmutableBasicUserType.INSTANCE);
        basicUserTypes.put(BigInteger.class, ImmutableBasicUserType.INSTANCE);
        basicUserTypes.put(BigDecimal.class, ImmutableBasicUserType.INSTANCE);
        basicUserTypes.put(String.class, ImmutableBasicUserType.INSTANCE);

        basicUserTypes.put(Locale.class, ImmutableBasicUserType.INSTANCE);

        basicUserTypes.put(java.util.Date.class, DateBasicUserType.INSTANCE);
        basicUserTypes.put(java.sql.Date.class, DateBasicUserType.INSTANCE);
        basicUserTypes.put(java.sql.Time.class, DateBasicUserType.INSTANCE);
        basicUserTypes.put(java.sql.Timestamp.class, TimestampBasicUserType.INSTANCE);

        basicUserTypes.put(java.util.Calendar.class, CalendarBasicUserType.INSTANCE);
        basicUserTypes.put(java.util.GregorianCalendar.class, CalendarBasicUserType.INSTANCE);

        // Java 8 time types
        try {
            basicUserTypes.put(Class.forName("java.time.LocalDate"), ImmutableBasicUserType.INSTANCE);
            basicUserTypes.put(Class.forName("java.time.LocalTime"), ImmutableBasicUserType.INSTANCE);
            basicUserTypes.put(Class.forName("java.time.LocalDateTime"), ImmutableBasicUserType.INSTANCE);
            basicUserTypes.put(Class.forName("java.time.OffsetTime"), ImmutableBasicUserType.INSTANCE);
            basicUserTypes.put(Class.forName("java.time.OffsetDateTime"), ImmutableBasicUserType.INSTANCE);
            basicUserTypes.put(Class.forName("java.time.ZonedDateTime"), ImmutableBasicUserType.INSTANCE);
            basicUserTypes.put(Class.forName("java.time.Duration"), ImmutableBasicUserType.INSTANCE);
            basicUserTypes.put(Class.forName("java.time.Instant"), ImmutableBasicUserType.INSTANCE);
        } catch (ClassNotFoundException ex) {
            // If they aren't found, we ignore them
        }
    }

    @Override
    public <X> void registerBasicUserType(Class<X> clazz, BasicUserType<X> userType) {
        basicUserTypes.put(clazz, userType);
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
                // Enums are always considered immutable
                userType = ImmutableBasicUserType.INSTANCE;
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
