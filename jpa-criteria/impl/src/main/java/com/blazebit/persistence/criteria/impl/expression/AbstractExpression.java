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

package com.blazebit.persistence.criteria.impl.expression;

import com.blazebit.persistence.criteria.BlazeExpression;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.expression.function.FunctionExpressionImpl;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractExpression<T> extends AbstractSelection<T> implements BlazeExpression<T> {

    private static final long serialVersionUID = 1L;

    private static final Map<Class<?>, String> CAST_SUFFIXES;

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

        CAST_SUFFIXES = Collections.unmodifiableMap(s);
    }

    public AbstractExpression(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> javaType) {
        super(criteriaBuilder, javaType);
    }

    private static String castSuffix(Class<?> t) {
        String suffix = CAST_SUFFIXES.get(t);

        if (suffix == null) {
            return t.getSimpleName().toUpperCase(Locale.ROOT);
        }

        return suffix;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <X> BlazeExpression<X> as(Class<X> type) {
        return type.equals(getJavaType()) ? (BlazeExpression<X>) this : new FunctionExpressionImpl<>(criteriaBuilder, type, "CAST_" + castSuffix(type), this);
    }

    @Override
    public Predicate isNull() {
        return criteriaBuilder.isNull(this);
    }

    @Override
    public Predicate isNotNull() {
        return criteriaBuilder.isNotNull(this);
    }

    @Override
    public Predicate in(Object... values) {
        List<Expression<? extends T>> valueList = new ArrayList<>(values.length);
        for (Object value : values) {
            if (value instanceof Expression<?>) {
                valueList.add((Expression<? extends T>) value);
            } else if (value == null) {
                valueList.add(criteriaBuilder.nullValue(getJavaType()));
            } else {
                valueList.add(criteriaBuilder.value((T) value));
            }
        }
        return new InPredicate<T>(criteriaBuilder, this, valueList);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Predicate in(Expression<?>... values) {
        List<Expression<? extends T>> valueList = new ArrayList<>(values.length);
        for (Expression<?> value : values) {
            valueList.add((Expression<? extends T>) value);
        }
        return new InPredicate<T>(criteriaBuilder, this, valueList);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Predicate in(Collection<?> values) {
        List<Expression<? extends T>> valueList = new ArrayList<>(values.size());
        for (Object value : values) {
            if (value instanceof Expression<?>) {
                valueList.add((Expression<? extends T>) value);
            } else if (value == null) {
                valueList.add(criteriaBuilder.nullValue(getJavaType()));
            } else {
                valueList.add((Expression<? extends T>) criteriaBuilder.value(value));
            }
        }
        return new InPredicate<T>(criteriaBuilder, this, valueList);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Predicate in(Expression<Collection<?>> values) {
        if (values instanceof LiteralExpression<?>) {
            Collection<?> collection = ((LiteralExpression<Collection<?>>) values).getLiteral();
            List<Expression<? extends T>> valueList = new ArrayList<>(collection.size());
            for (Object value : collection) {
                if (value instanceof Expression<?>) {
                    valueList.add((Expression<? extends T>) value);
                } else if (value == null) {
                    valueList.add(criteriaBuilder.nullLiteral(getJavaType()));
                } else {
                    valueList.add((Expression<? extends T>) criteriaBuilder.literal(value));
                }
            }
            return new InPredicate<T>(criteriaBuilder, this, valueList);
        }
        List<Expression<? extends T>> valueList = new ArrayList<>(1);
        valueList.add((Expression<? extends T>) values);
        return new InPredicate<T>(criteriaBuilder, this, valueList);
    }

    @SuppressWarnings({"unchecked"})
    public BlazeExpression<Long> asLong() {
        setJavaType(Long.class);
        return (BlazeExpression<Long>) this;
    }

    @SuppressWarnings({"unchecked"})
    public BlazeExpression<Integer> asInteger() {
        setJavaType(Integer.class);
        return (BlazeExpression<Integer>) this;
    }

    @SuppressWarnings({"unchecked"})
    public BlazeExpression<Float> asFloat() {
        setJavaType(Float.class);
        return (BlazeExpression<Float>) this;
    }

    @SuppressWarnings({"unchecked"})
    public BlazeExpression<Double> asDouble() {
        setJavaType(Double.class);
        return (BlazeExpression<Double>) this;
    }

    @SuppressWarnings({"unchecked"})
    public BlazeExpression<BigDecimal> asBigDecimal() {
        setJavaType(BigDecimal.class);
        return (BlazeExpression<BigDecimal>) this;
    }

    @SuppressWarnings({"unchecked"})
    public BlazeExpression<BigInteger> asBigInteger() {
        setJavaType(BigInteger.class);
        return (BlazeExpression<BigInteger>) this;
    }

    @SuppressWarnings({"unchecked"})
    public BlazeExpression<String> asString() {
        setJavaType(String.class);
        return (BlazeExpression<String>) this;
    }
}
