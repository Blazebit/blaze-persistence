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

package com.blazebit.persistence.parser.expression;

import com.blazebit.persistence.parser.predicate.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
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
    public PathExpression createPathExpression(final String expression, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.PathExpression", delegate, expression, false, macroConfiguration, ExpressionCache.PATH_EXPRESSION_SUPPLIER);
    }

    @Override
    public Expression createJoinPathExpression(final String expression, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.JoinPathExpression", delegate, expression, false, macroConfiguration, ExpressionCache.JOIN_PATH_EXPRESSION_SUPPLIER);
    }

    @Override
    public Expression createSimpleExpression(final String expression, final boolean allowQuantifiedPredicates, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.SimpleExpression", delegate, expression, allowQuantifiedPredicates, macroConfiguration, ExpressionCache.SIMPLE_EXPRESSION_SUPPLIER);
    }

    @Override
    public Expression createCaseOperandExpression(final String expression, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.CaseOperandExpression", delegate, expression, false, macroConfiguration, ExpressionCache.CASE_OPERAND_EXPRESSION_SUPPLIER);
    }

    @Override
    public Expression createScalarExpression(final String expression, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.ScalarExpression", delegate, expression, false, macroConfiguration, ExpressionCache.SCALAR_EXPRESSION_SUPPLIER);
    }

    @Override
    public Expression createArithmeticExpression(final String expression, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.ArithmeticExpression", delegate, expression, false, macroConfiguration, ExpressionCache.ARITHMETIC_EXPRESSION_SUPPLIER);
    }

    @Override
    public Expression createStringExpression(final String expression, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.StringExpression", delegate, expression, false, macroConfiguration, ExpressionCache.STRING_EXPRESSION_SUPPLIER);
    }

    @Override
    public Expression createOrderByExpression(final String expression, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.OrderByExpression", delegate, expression, false, macroConfiguration, ExpressionCache.ORDER_BY_EXPRESSION_SUPPLIER);
    }

    @Override
    public List<Expression> createInItemExpressions(final String[] parameterOrLiteralExpressions, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        if (parameterOrLiteralExpressions == null) {
            throw new NullPointerException("parameterOrLiteralExpressions");
        }
        if (parameterOrLiteralExpressions.length == 0) {
            throw new IllegalArgumentException("empty parameterOrLiteralExpressions");
        }

        List<Expression> inItemExpressions = new ArrayList<Expression>();

        if (parameterOrLiteralExpressions.length == 1) {
            inItemExpressions.add(createInItemOrPathExpression(parameterOrLiteralExpressions[0], macroConfiguration, usedMacros));
        } else {
            for (final String parameterOrLiteralExpression : parameterOrLiteralExpressions) {
                inItemExpressions.add(createInItemExpression(parameterOrLiteralExpression, macroConfiguration, usedMacros));
            }
        }

        return inItemExpressions;
    }

    @Override
    public Expression createInItemExpression(final String parameterOrLiteralExpression, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.InPredicateExpression", delegate, parameterOrLiteralExpression, false, macroConfiguration, ExpressionCache.IN_ITEM_EXPRESSION_SUPPLIER);
    }

    @Override
    public Expression createInItemOrPathExpression(final String parameterOrLiteralExpression, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.InPredicateSingleExpression", delegate, parameterOrLiteralExpression, false, macroConfiguration, ExpressionCache.IN_ITEM_OR_PATH_EXPRESSION_SUPPLIER);
    }

    @Override
    public Predicate createBooleanExpression(final String expression, final boolean allowQuantifiedPredicates, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionCache.getOrDefault("com.blazebit.persistence.parser.expression.cache.PredicateExpression", delegate, expression, allowQuantifiedPredicates, macroConfiguration, ExpressionCache.BOOLEAN_EXPRESSION_SUPPLIER);
    }
}
