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

package com.blazebit.persistence.criteria.impl.expression;

import com.blazebit.persistence.criteria.BlazeExpression;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.expression.function.CastFunction;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractExpression<T> extends AbstractSelection<T> implements BlazeExpression<T> {

    private static final long serialVersionUID = 1L;

    public AbstractExpression(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> javaType) {
        super(criteriaBuilder, javaType);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <X> BlazeExpression<X> as(Class<X> type) {
        return type.equals(getJavaType()) ? (BlazeExpression<X>) this : new CastFunction<X, T>(criteriaBuilder, type, this);
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
        return criteriaBuilder.in(this, values);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Predicate in(Expression<?>... values) {
        return criteriaBuilder.in(this, (Expression<? extends T>[]) values);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Predicate in(Collection<?> values) {
        return criteriaBuilder.in(this, (Collection<T>) values);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Predicate in(Expression<Collection<?>> values) {
        return criteriaBuilder.in(this, values);
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
