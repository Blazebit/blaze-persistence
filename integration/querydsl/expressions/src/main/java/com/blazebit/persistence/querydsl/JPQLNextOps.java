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

package com.blazebit.persistence.querydsl;

import com.querydsl.core.types.Operator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * {@code JPQLNextOps} provides JPQL.Next specific operators.
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
public enum JPQLNextOps implements Operator {
    PAGE_POSITION(Long.class),
    ENTITY_FUNCTION(Object.class),
    SET_UNION(Object.class),
    SET_UNION_ALL(Object.class),
    SET_INTERSECT(Object.class),
    SET_INTERSECT_ALL(Object.class),
    SET_EXCEPT(Object.class),
    SET_EXCEPT_ALL(Object.class),
    LEFT_NESTED_SET_UNION(Object.class),
    LEFT_NESTED_SET_UNION_ALL(Object.class),
    LEFT_NESTED_SET_INTERSECT(Object.class),
    LEFT_NESTED_SET_INTERSECT_ALL(Object.class),
    LEFT_NESTED_SET_EXCEPT(Object.class),
    LEFT_NESTED_SET_EXCEPT_ALL(Object.class),
    GROUP_CONCAT(String.class),
    WINDOW_GROUP_CONCAT(String.class),
    GREATEST(Object.class),
    LEAST(Object.class),
    REPEAT(String.class),
    ROW_VALUES(Boolean.class),
    ROW_NUMBER(Object.class),
    RANK(Object.class),
    DENSE_RANK(Object.class),
    PERCENT_RANK(Object.class),
    CUME_DIST(Object.class),
    NTILE(Object.class),
    LEAD(Object.class),
    LAG(Object.class),
    FIRST_VALUE(Object.class),
    LAST_VALUE(Object.class),
    NTH_VALUE(Object.class),
    WITH_ALIAS(Object.class),
    WITH_RECURSIVE_ALIAS(Object.class),
    WITH_COLUMNS(Object.class),
    WITH_RECURSIVE_COLUMNS(Object.class),
    BIND(Object.class),

    CAST_BOOLEAN(Boolean.class),
    CAST_BYTE(Byte.class),
    CAST_SHORT(Short.class),
    CAST_INTEGER(Integer.class),
    CAST_LONG(Long.class),
    CAST_FLOAT(Float.class),
    CAST_DOUBLE(Double.class),
    CAST_CHARACTER(Character.class),
    CAST_STRING(String.class),
    CAST_BIGINTEGER(BigInteger.class),
    CAST_BIGDECIMAL(BigDecimal.class),
    CAST_TIME(Time.class),
    CAST_DATE(Date.class),
    CAST_TIMESTAMP(Timestamp.class),
    CAST_CALENDAR(Calendar.class),

    TREAT_BOOLEAN(Boolean.class),
    TREAT_BYTE(Byte.class),
    TREAT_SHORT(Short.class),
    TREAT_INTEGER(Integer.class),
    TREAT_LONG(Long.class),
    TREAT_FLOAT(Float.class),
    TREAT_DOUBLE(Double.class),
    TREAT_CHARACTER(Character.class),
    TREAT_STRING(String.class),
    TREAT_BIGINTEGER(BigInteger.class),
    TREAT_BIGDECIMAL(BigDecimal.class),
    TREAT_TIME(Time.class),
    TREAT_DATE(Date.class),
    TREAT_TIMESTAMP(Timestamp.class),
    TREAT_CALENDAR(Calendar.class),
    TREAT_INSTANT(Instant.class),
    TREAT_LOCALDATE(LocalDate.class),
    TREAT_LOCALDATETIME(LocalDateTime.class),
    TREAT_LOCALTIME(LocalTime.class),
    TREAT_OFFSETDATETIME(OffsetDateTime.class),
    TREAT_OFFSETTIME(OffsetTime.class),
    TREAT_ZONEDDATETIME(ZonedDateTime.class),

    LITERAL_TIME(Time.class),
    LITERAL_DATE(Date.class),
    LITERAL_TIMESTAMP(Timestamp.class),
    LITERAL_CALENDAR(Calendar.class),
    LITERAL_INSTANT(Instant.class),
    LITERAL_LOCALDATE(LocalDate.class),
    LITERAL_LOCALDATETIME(LocalDateTime.class),
    LITERAL_LOCALTIME(LocalTime.class),
    LITERAL_OFFSETDATETIME(OffsetDateTime.class),
    LITERAL_OFFSETTIME(OffsetTime.class),
    LITERAL_ZONEDDATETIME(ZonedDateTime.class),

    WINDOW_NAME(Object.class),
    WINDOW_BASE(Object.class),
    WINDOW_DEFINITION_1(Object.class), // base window name, partition by, order by or range clause
    WINDOW_DEFINITION_2(Object.class), // two of base window name, partition by, order by or range clause
    WINDOW_DEFINITION_3(Object.class), // three of  base window name, partition by, order by or range clause
    WINDOW_DEFINITION_4(Object.class), //  base window name, partition by, order by and range clause
    WINDOW_ORDER_BY(Object.class),
    WINDOW_PARTITION_BY(Object.class),
    WINDOW_ROWS(Object.class),
    WINDOW_RANGE(Object.class),
    WINDOW_GROUPS(Object.class),
    WINDOW_BETWEEN(Object.class),
    WINDOW_UNBOUNDED_PRECEDING(Object.class),
    WINDOW_PRECEDING(Object.class),
    WINDOW_FOLLOWING(Object.class),
    WINDOW_UNBOUNDED_FOLLOWING(Object.class),
    WINDOW_CURRENT_ROW(Object.class),

    JSON_GET(Object.class),

    FILTER(Object.class);

    public static final Set<JPQLNextOps> LEFT_NESTED_SET_OPERATIONS = Collections.unmodifiableSet(EnumSet.of(
            JPQLNextOps.LEFT_NESTED_SET_UNION,
            JPQLNextOps.LEFT_NESTED_SET_UNION_ALL,
            JPQLNextOps.LEFT_NESTED_SET_INTERSECT,
            JPQLNextOps.LEFT_NESTED_SET_INTERSECT_ALL,
            JPQLNextOps.LEFT_NESTED_SET_EXCEPT,
            JPQLNextOps.LEFT_NESTED_SET_EXCEPT_ALL
    ));

    public static final Set<JPQLNextOps> SET_OPERATIONS = Collections.unmodifiableSet(EnumSet.of(
            JPQLNextOps.SET_UNION,
            JPQLNextOps.SET_UNION_ALL,
            JPQLNextOps.SET_INTERSECT,
            JPQLNextOps.SET_INTERSECT_ALL,
            JPQLNextOps.SET_EXCEPT,
            JPQLNextOps.SET_EXCEPT_ALL,
            JPQLNextOps.LEFT_NESTED_SET_UNION,
            JPQLNextOps.LEFT_NESTED_SET_UNION_ALL,
            JPQLNextOps.LEFT_NESTED_SET_INTERSECT,
            JPQLNextOps.LEFT_NESTED_SET_INTERSECT_ALL,
            JPQLNextOps.LEFT_NESTED_SET_EXCEPT,
            JPQLNextOps.LEFT_NESTED_SET_EXCEPT_ALL
    ));

    private final Class<?> type;

    private JPQLNextOps(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return this.type;
    }

}
