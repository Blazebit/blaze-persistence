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

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractExpressionFactoryMacroAdapter implements ExpressionFactory {
    @Override
    public MacroConfiguration getDefaultMacroConfiguration() {
        return null;
    }

    @Override
    public PathExpression createPathExpression(String expression) {
        return createPathExpression(expression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createJoinPathExpression(String expression) {
        return createJoinPathExpression(expression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowQuantifiedPredicates) {
        return createSimpleExpression(expression, allowQuantifiedPredicates, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createCaseOperandExpression(String caseOperandExpression) {
        return createCaseOperandExpression(caseOperandExpression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createScalarExpression(String expression) {
        return createScalarExpression(expression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createArithmeticExpression(String expression) {
        return createArithmeticExpression(expression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createStringExpression(String expression) {
        return createStringExpression(expression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createOrderByExpression(String expression) {
        return createOrderByExpression(expression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public List<Expression> createInItemExpressions(String[] parameterOrLiteralExpressions) {
        return createInItemExpressions(parameterOrLiteralExpressions, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createInItemExpression(String parameterOrLiteralExpression) {
        return createInItemExpression(parameterOrLiteralExpression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createInItemOrPathExpression(String parameterOrLiteralExpression) {
        return createInItemOrPathExpression(parameterOrLiteralExpression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Predicate createBooleanExpression(String expression, boolean allowQuantifiedPredicates) {
        return createBooleanExpression(expression, allowQuantifiedPredicates, getDefaultMacroConfiguration(), null);
    }

}
