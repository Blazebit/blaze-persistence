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

package com.blazebit.persistence.criteria.impl.expression.function;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.expression.AbstractExpression;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CastFunction<T, Y> extends FunctionFunction<T> {

    private static final long serialVersionUID = 1L;

    private static final Map<Class<?>, String> SUFFIXES;

    public CastFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> javaType, AbstractExpression<Y> expression) {
        super(criteriaBuilder, javaType, "CAST_" + castSuffix(javaType), expression);
    }

    static {
        Map<Class<?>, String> s = new HashMap<Class<?>, String>();
        s.put(Boolean.class, "BOOLEAN");
        s.put(Boolean.TYPE, "BOOLEAN");
        s.put(Byte.class, "BYTE");
        s.put(Byte.TYPE, "BYTE");
        s.put(Short.class, "SHORT");
        s.put(Short.TYPE, "SHORT");
        s.put(Integer.class, "INTEGER");
        s.put(Integer.TYPE, "INTEGER");
        s.put(Long.class, "LONG");
        s.put(Long.TYPE, "LONG");

        s.put(Float.class, "FLOAT");
        s.put(Float.TYPE, "FLOAT");
        s.put(Double.class, "DOUBLE");
        s.put(Double.TYPE, "DOUBLE");

        s.put(Character.class, "CHARACTER");
        s.put(Character.TYPE, "CHARACTER");

        s.put(String.class, "STRING");
        s.put(BigInteger.class, "BIGINTEGER");
        s.put(BigDecimal.class, "BIGDECIMAL");
        s.put(Time.class, "TIME");
        s.put(Date.class, "DATE");
        s.put(Timestamp.class, "TIMESTAMP");
        s.put(java.util.Date.class, "TIMESTAMP");
        s.put(java.util.Calendar.class, "CALENDAR");

        SUFFIXES = Collections.unmodifiableMap(s);
    }

    private static String castSuffix(Class<?> t) {
        String suffix = SUFFIXES.get(t);

        if (suffix == null) {
            throw new IllegalArgumentException("Unsupported cast type: " + t.getName());
        }

        return suffix;
    }
}
