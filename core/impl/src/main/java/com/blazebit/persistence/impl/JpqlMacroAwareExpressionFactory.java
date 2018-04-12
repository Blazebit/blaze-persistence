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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.MacroConfiguration;
import com.blazebit.persistence.parser.expression.PathExpression;
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
    public PathExpression createPathExpression(String expression) {
        return expressionFactory.createPathExpression(expression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public PathExpression createPathExpression(String expression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionFactory.createPathExpression(expression, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createJoinPathExpression(String expression) {
        return expressionFactory.createJoinPathExpression(expression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createJoinPathExpression(String expression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionFactory.createJoinPathExpression(expression, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowQuantifiedPredicates) {
        return expressionFactory.createSimpleExpression(expression, allowQuantifiedPredicates, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionFactory.createSimpleExpression(expression, allowQuantifiedPredicates, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createCaseOperandExpression(String caseOperandExpression) {
        return expressionFactory.createCaseOperandExpression(caseOperandExpression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createCaseOperandExpression(String caseOperandExpression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionFactory.createCaseOperandExpression(caseOperandExpression, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createScalarExpression(String expression) {
        return expressionFactory.createScalarExpression(expression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createScalarExpression(String expression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionFactory.createScalarExpression(expression, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createArithmeticExpression(String expression) {
        return expressionFactory.createArithmeticExpression(expression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createArithmeticExpression(String expression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionFactory.createArithmeticExpression(expression, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createStringExpression(String expression) {
        return expressionFactory.createStringExpression(expression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createStringExpression(String expression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionFactory.createStringExpression(expression, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createOrderByExpression(String expression) {
        return expressionFactory.createOrderByExpression(expression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createOrderByExpression(String expression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return expressionFactory.createOrderByExpression(expression, macroConfiguration, usedMacros);
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
