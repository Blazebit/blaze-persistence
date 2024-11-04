/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

import com.blazebit.persistence.parser.predicate.Predicate;

import java.util.List;
import java.util.Set;

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
        return (PathExpression) createPathExpression(expression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createSimpleExpression(String expression) {
        return createSimpleExpression(expression, false, false, false, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowQuantifiedPredicates) {
        return createSimpleExpression(expression, false, allowQuantifiedPredicates, false, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowOuter, boolean allowQuantifiedPredicates, boolean allowObjectExpression) {
        return createSimpleExpression(expression, allowOuter, allowQuantifiedPredicates, allowObjectExpression, getDefaultMacroConfiguration(), null);
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowOuter, boolean allowQuantifiedPredicates) {
        return createSimpleExpression(expression, allowOuter, allowQuantifiedPredicates, false, getDefaultMacroConfiguration(), null);
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
