/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.expression.ArrayExpression;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.MacroConfiguration;
import com.blazebit.persistence.parser.expression.PathElementExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.parser.predicate.Predicate;

import java.util.List;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class JpqlMacroAwareExpressionFactory implements ExpressionFactory {

    private final ExpressionFactory expressionFactory;
    private final JpqlMacroStorage macroStorage;

    public JpqlMacroAwareExpressionFactory(ExpressionFactory expressionFactory, JpqlMacroStorage macroStorage) {
        this.expressionFactory = expressionFactory;
        this.macroStorage = macroStorage;
    }

    @Override
    public <T extends ExpressionFactory> T unwrap(Class<T> clazz) {
        if (JpqlMacroAwareExpressionFactory.class.isAssignableFrom(clazz)) {
            return (T) this;
        }
        return expressionFactory.unwrap(clazz);
    }

    @Override
    public MacroConfiguration getDefaultMacroConfiguration() {
        return macroStorage.getMacroConfiguration();
    }

    @Override
    public Expression createJoinPathExpression(String expression) {
        return createJoinPathExpression(expression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createJoinPathExpression(String expression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        Expression pathExpression = createPathExpression(expression, macroConfiguration, usedMacros);
        if (pathExpression instanceof PathExpression) {
            List<PathElementExpression> expressions = ((PathExpression) pathExpression).getExpressions();
            PathElementExpression first;
            if (expressions.size() > 1 || (first = expressions.get(0)) instanceof PropertyExpression || first instanceof ArrayExpression) {
                return pathExpression;
            }
            return first;
        }
        return pathExpression;
    }

    @Override
    public PathExpression createPathExpression(String expression) {
        return (PathExpression) expressionFactory.createPathExpression(expression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createPathExpression(String expression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionFactory.createPathExpression(expression, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createSimpleExpression(String expression) {
        return expressionFactory.createSimpleExpression(expression, false, false, false, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowQuantifiedPredicates) {
        return expressionFactory.createSimpleExpression(expression, false, allowQuantifiedPredicates, false, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowOuter, boolean allowQuantifiedPredicates) {
        return expressionFactory.createSimpleExpression(expression, allowOuter, allowQuantifiedPredicates, false, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowOuter, boolean allowQuantifiedPredicates, boolean allowObjectExpression) {
        return expressionFactory.createSimpleExpression(expression, allowOuter, allowQuantifiedPredicates, allowObjectExpression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowOuter, boolean allowQuantifiedPredicates, boolean allowObjectExpression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionFactory.createSimpleExpression(expression, allowOuter, allowQuantifiedPredicates, allowObjectExpression, macroConfiguration, usedMacros);
    }

    @Override
    public List<Expression> createInItemExpressions(String[] parameterOrLiteralExpressions) {
        return expressionFactory.createInItemExpressions(parameterOrLiteralExpressions, getDefaultMacroConfiguration(), null);
    }

    @Override
    public List<Expression> createInItemExpressions(String[] parameterOrLiteralExpressions, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionFactory.createInItemExpressions(parameterOrLiteralExpressions, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createInItemExpression(String parameterOrLiteralExpression) {
        return expressionFactory.createInItemExpression(parameterOrLiteralExpression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createInItemExpression(String parameterOrLiteralExpression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionFactory.createInItemExpression(parameterOrLiteralExpression, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createInItemOrPathExpression(String parameterOrLiteralExpression) {
        return expressionFactory.createInItemOrPathExpression(parameterOrLiteralExpression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createInItemOrPathExpression(String parameterOrLiteralExpression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionFactory.createInItemOrPathExpression(parameterOrLiteralExpression, macroConfiguration, usedMacros);
    }

    @Override
    public Predicate createBooleanExpression(String expression, boolean allowQuantifiedPredicates) {
        return expressionFactory.createBooleanExpression(expression, allowQuantifiedPredicates, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Predicate createBooleanExpression(String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionFactory.createBooleanExpression(expression, allowQuantifiedPredicates, macroConfiguration, usedMacros);
    }

    /*  WARNING: Be careful when changing the implementation of equals and hashCode. Extensions rely on the the logic for efficient caching.  */

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ExpressionFactory) {
            ExpressionFactory that = (ExpressionFactory) o;
            ExpressionFactory thatExpressionFactory = that.unwrap(expressionFactory.getClass());
            if (thatExpressionFactory == null || !expressionFactory.equals(thatExpressionFactory)) {
                return false;
            }
            return getDefaultMacroConfiguration() != null ? getDefaultMacroConfiguration().equals(that.getDefaultMacroConfiguration()) : that.getDefaultMacroConfiguration() == null;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = expressionFactory != null ? expressionFactory.hashCode() : 0;
        result = 31 * result + (getDefaultMacroConfiguration() != null ? getDefaultMacroConfiguration().hashCode() : 0);
        return result;
    }
}
