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

package com.blazebit.persistence.impl.expression;

import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.expression.ExpressionCache.Supplier;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractCachingExpressionFactory extends AbstractExpressionFactoryMacroAdapter {

    private final ExpressionFactory delegate;
    private final ExpressionCache expressionCache;

    public AbstractCachingExpressionFactory(ExpressionFactory delegate, ExpressionCache expressionCache) {
        this.delegate = delegate;
        this.expressionCache = expressionCache;
    }

    @Override
    public <T extends ExpressionFactory> T unwrap(Class<T> clazz) {
        if (AbstractCachingExpressionFactory.class.isAssignableFrom(clazz)) {
            return (T) this;
        }
        return delegate.unwrap(clazz);
    }

    @Override
    public MacroConfiguration getDefaultMacroConfiguration() {
        return delegate.getDefaultMacroConfiguration();
    }

    public ExpressionCache getExpressionCache() {
        return expressionCache;
    }

    @Override
    public PathExpression createPathExpression(final String expression, final MacroConfiguration macroConfiguration) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.PathExpression", expression, macroConfiguration, new Supplier<PathExpression>() {

            @Override
            public PathExpression get() {
                return delegate.createPathExpression(expression, macroConfiguration);
            }

        });
    }

    @Override
    public Expression createJoinPathExpression(final String expression, final MacroConfiguration macroConfiguration) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.JoinPathExpression", expression, macroConfiguration, new Supplier<Expression>() {

            @Override
            public Expression get() {
                return delegate.createJoinPathExpression(expression, macroConfiguration);
            }

        });
    }

    @Override
    public Expression createSimpleExpression(final String expression, final boolean allowQuantifiedPredicates, final MacroConfiguration macroConfiguration) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.SimpleExpression", expression, macroConfiguration, new Supplier<Expression>() {

            @Override
            public Expression get() {
                return delegate.createSimpleExpression(expression, allowQuantifiedPredicates, macroConfiguration);
            }

        });
    }

    @Override
    public Expression createCaseOperandExpression(final String expression, final MacroConfiguration macroConfiguration) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.CaseOperandExpression", expression, macroConfiguration, new Supplier<Expression>() {

            @Override
            public Expression get() {
                return delegate.createCaseOperandExpression(expression, macroConfiguration);
            }

        });
    }

    @Override
    public Expression createScalarExpression(final String expression, final MacroConfiguration macroConfiguration) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.ScalarExpression", expression, macroConfiguration, new Supplier<Expression>() {

            @Override
            public Expression get() {
                return delegate.createScalarExpression(expression, macroConfiguration);
            }

        });
    }

    @Override
    public Expression createArithmeticExpression(final String expression, final MacroConfiguration macroConfiguration) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.ArithmeticExpression", expression, macroConfiguration, new Supplier<Expression>() {

            @Override
            public Expression get() {
                return delegate.createArithmeticExpression(expression, macroConfiguration);
            }

        });
    }

    @Override
    public Expression createStringExpression(final String expression, final MacroConfiguration macroConfiguration) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.StringExpression", expression, macroConfiguration, new Supplier<Expression>() {

            @Override
            public Expression get() {
                return delegate.createStringExpression(expression, macroConfiguration);
            }

        });
    }

    @Override
    public Expression createOrderByExpression(final String expression, final MacroConfiguration macroConfiguration) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.OrderByExpression", expression, macroConfiguration, new Supplier<Expression>() {

            @Override
            public Expression get() {
                return delegate.createOrderByExpression(expression, macroConfiguration);
            }

        });
    }

    @Override
    public List<Expression> createInItemExpressions(final String[] parameterOrLiteralExpressions, final MacroConfiguration macroConfiguration) {
        if (parameterOrLiteralExpressions == null) {
            throw new NullPointerException("parameterOrLiteralExpressions");
        }
        if (parameterOrLiteralExpressions.length == 0) {
            throw new IllegalArgumentException("empty parameterOrLiteralExpressions");
        }

        List<Expression> inItemExpressions = new ArrayList<Expression>();

        if (parameterOrLiteralExpressions.length == 1) {
            inItemExpressions.add(createInItemOrPathExpression(parameterOrLiteralExpressions[0], macroConfiguration));
        } else {
            for (final String parameterOrLiteralExpression : parameterOrLiteralExpressions) {
                inItemExpressions.add(createInItemExpression(parameterOrLiteralExpression, macroConfiguration));
            }
        }

        return inItemExpressions;
    }

    @Override
    public Expression createInItemExpression(final String parameterOrLiteralExpression, final MacroConfiguration macroConfiguration) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.InPredicateExpression", parameterOrLiteralExpression, macroConfiguration, new Supplier<Expression>() {

            @Override
            public Expression get() {
                return delegate.createInItemExpression(parameterOrLiteralExpression, macroConfiguration);
            }

        });
    }

    @Override
    public Expression createInItemOrPathExpression(final String parameterOrLiteralExpression, final MacroConfiguration macroConfiguration) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.InPredicateSingleExpression", parameterOrLiteralExpression, macroConfiguration, new Supplier<Expression>() {

            @Override
            public Expression get() {
                return delegate.createInItemOrPathExpression(parameterOrLiteralExpression, macroConfiguration);
            }

        });
    }

    @Override
    public Predicate createBooleanExpression(final String expression, final boolean allowQuantifiedPredicates, final MacroConfiguration macroConfiguration) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.PredicateExpression", expression, macroConfiguration, new Supplier<Predicate>() {

            @Override
            public Predicate get() {
                return delegate.createBooleanExpression(expression, allowQuantifiedPredicates, macroConfiguration);
            }

        });
    }
}
