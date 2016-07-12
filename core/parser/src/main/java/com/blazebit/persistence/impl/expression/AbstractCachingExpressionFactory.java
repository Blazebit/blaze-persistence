/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.impl.expression;

import com.blazebit.persistence.impl.predicate.Predicate;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractCachingExpressionFactory implements ExpressionFactory {

    private final ExpressionFactory delegate;

    public AbstractCachingExpressionFactory(ExpressionFactory delegate) {
        this.delegate = delegate;
    }

    protected static interface Supplier<T> {

        public T get();
    }

    protected abstract <E extends Expression> E getOrDefault(String cacheName, String cacheKey, Supplier<E> defaultSupplier);

    protected abstract <E extends Expression> E getOrDefault(String cacheName, Object[] cacheKey, Supplier<E> defaultSupplier);

    @Override
    public PathExpression createPathExpression(final String expression) {
        return getOrDefault("com.blazebit.persistence.parser.expression.cache.PathExpression", expression, new Supplier<PathExpression>() {

            @Override
            public PathExpression get() {
                return delegate.createPathExpression(expression);
            }

        });
    }

    @Override
    public Expression createSimpleExpression(final String expression) {
        return getOrDefault("com.blazebit.persistence.parser.expression.cache.SimpleExpression", expression, new Supplier<Expression>() {

            @Override
            public Expression get() {
                return delegate.createSimpleExpression(expression);
            }

        });
    }

    @Override
    public Expression createCaseOperandExpression(final String expression) {
        return getOrDefault("com.blazebit.persistence.parser.expression.cache.CaseOperandExpression", expression, new Supplier<Expression>() {

            @Override
            public Expression get() {
                return delegate.createCaseOperandExpression(expression);
            }

        });
    }

    @Override
    public Expression createScalarExpression(final String expression) {
        return getOrDefault("com.blazebit.persistence.parser.expression.cache.ScalarExpression", expression, new Supplier<Expression>() {

            @Override
            public Expression get() {
                return delegate.createScalarExpression(expression);
            }

        });
    }

    @Override
    public Expression createArithmeticExpression(final String expression) {
        return getOrDefault("com.blazebit.persistence.parser.expression.cache.ArithmeticExpression", expression, new Supplier<Expression>() {

            @Override
            public Expression get() {
                return delegate.createArithmeticExpression(expression);
            }

        });
    }

    @Override
    public Expression createStringExpression(final String expression) {
        return getOrDefault("com.blazebit.persistence.parser.expression.cache.StringExpression", expression, new Supplier<Expression>() {

            @Override
            public Expression get() {
                return delegate.createStringExpression(expression);
            }

        });
    }

    @Override
    public Expression createOrderByExpression(final String expression) {
        return getOrDefault("com.blazebit.persistence.parser.expression.cache.OrderByExpression", expression, new Supplier<Expression>() {

            @Override
            public Expression get() {
                return delegate.createOrderByExpression(expression);
            }

        });
    }

    @Override
    public Expression createInPredicateExpression(final String[] parameterOrLiteralExpressions) {
        return getOrDefault("com.blazebit.persistence.parser.expression.cache.InPredicateExpression", parameterOrLiteralExpressions, new Supplier<Expression>() {

            @Override
            public Expression get() {
                return delegate.createInPredicateExpression(parameterOrLiteralExpressions);
            }

        });
    }

    @Override
    public Predicate createPredicateExpression(final String expression, final boolean allowQuantifiedPredicates) {
        return getOrDefault("com.blazebit.persistence.parser.expression.cache.PredicateExpression", expression, new Supplier<Predicate>() {

            @Override
            public Predicate get() {
                return delegate.createPredicateExpression(expression, allowQuantifiedPredicates);
            }

        });
    }

}
