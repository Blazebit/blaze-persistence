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

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.impl.expression.AbstractExpressionFactoryMacroAdapter;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.MacroConfiguration;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.predicate.Predicate;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class MacroConfigurationExpressionFactory extends AbstractExpressionFactoryMacroAdapter {

    private final ExpressionFactory expressionFactory;
    private final MacroConfiguration macroConfiguration;

    public MacroConfigurationExpressionFactory(ExpressionFactory expressionFactory, MacroConfiguration macroConfiguration) {
        this.expressionFactory = expressionFactory;
        this.macroConfiguration = macroConfiguration;
    }

    @Override
    public MacroConfiguration getDefaultMacroConfiguration() {
        return macroConfiguration;
    }

    @Override
    public <T extends ExpressionFactory> T unwrap(Class<T> clazz) {
        if (MacroConfigurationExpressionFactory.class.isAssignableFrom(clazz)) {
            return (T) this;
        }
        return expressionFactory.unwrap(clazz);
    }

    @Override
    public PathExpression createPathExpression(String expression, MacroConfiguration macroConfiguration) {
        return expressionFactory.createPathExpression(expression, macroConfiguration);
    }

    @Override
    public Expression createJoinPathExpression(String expression, MacroConfiguration macroConfiguration) {
        return expressionFactory.createJoinPathExpression(expression, macroConfiguration);
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration) {
        return expressionFactory.createSimpleExpression(expression, allowQuantifiedPredicates, macroConfiguration);
    }

    @Override
    public Expression createCaseOperandExpression(String caseOperandExpression, MacroConfiguration macroConfiguration) {
        return expressionFactory.createCaseOperandExpression(caseOperandExpression, macroConfiguration);
    }

    @Override
    public Expression createScalarExpression(String expression, MacroConfiguration macroConfiguration) {
        return expressionFactory.createScalarExpression(expression, macroConfiguration);
    }

    @Override
    public Expression createArithmeticExpression(String expression, MacroConfiguration macroConfiguration) {
        return expressionFactory.createArithmeticExpression(expression, macroConfiguration);
    }

    @Override
    public Expression createStringExpression(String expression, MacroConfiguration macroConfiguration) {
        return expressionFactory.createStringExpression(expression, macroConfiguration);
    }

    @Override
    public Expression createOrderByExpression(String expression, MacroConfiguration macroConfiguration) {
        return expressionFactory.createOrderByExpression(expression, macroConfiguration);
    }

    @Override
    public List<Expression> createInItemExpressions(String[] parameterOrLiteralExpressions, MacroConfiguration macroConfiguration) {
        return expressionFactory.createInItemExpressions(parameterOrLiteralExpressions, macroConfiguration);
    }

    @Override
    public Expression createInItemExpression(String parameterOrLiteralExpression, MacroConfiguration macroConfiguration) {
        return expressionFactory.createInItemExpression(parameterOrLiteralExpression, macroConfiguration);
    }

    @Override
    public Expression createInItemOrPathExpression(String parameterOrLiteralExpression, MacroConfiguration macroConfiguration) {
        return expressionFactory.createInItemOrPathExpression(parameterOrLiteralExpression, macroConfiguration);
    }

    @Override
    public Predicate createBooleanExpression(String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration) {
        return expressionFactory.createBooleanExpression(expression, allowQuantifiedPredicates, macroConfiguration);
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
