package com.blazebit.persistence.impl.expression;

import com.blazebit.persistence.impl.predicate.Predicate;

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
        return createPathExpression(expression, getDefaultMacroConfiguration());
    }

    @Override
    public Expression createJoinPathExpression(String expression) {
        return createJoinPathExpression(expression, getDefaultMacroConfiguration());
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowQuantifiedPredicates) {
        return createSimpleExpression(expression, allowQuantifiedPredicates, getDefaultMacroConfiguration());
    }

    @Override
    public Expression createCaseOperandExpression(String caseOperandExpression) {
        return createCaseOperandExpression(caseOperandExpression, getDefaultMacroConfiguration());
    }

    @Override
    public Expression createScalarExpression(String expression) {
        return createScalarExpression(expression, getDefaultMacroConfiguration());
    }

    @Override
    public Expression createArithmeticExpression(String expression) {
        return createArithmeticExpression(expression, getDefaultMacroConfiguration());
    }

    @Override
    public Expression createStringExpression(String expression) {
        return createStringExpression(expression, getDefaultMacroConfiguration());
    }

    @Override
    public Expression createOrderByExpression(String expression) {
        return createOrderByExpression(expression, getDefaultMacroConfiguration());
    }

    @Override
    public List<Expression> createInItemExpressions(String[] parameterOrLiteralExpressions) {
        return createInItemExpressions(parameterOrLiteralExpressions, getDefaultMacroConfiguration());
    }

    @Override
    public Expression createInItemExpression(String parameterOrLiteralExpression) {
        return createInItemExpression(parameterOrLiteralExpression, getDefaultMacroConfiguration());
    }

    @Override
    public Expression createInItemOrPathExpression(String parameterOrLiteralExpression) {
        return createInItemOrPathExpression(parameterOrLiteralExpression, getDefaultMacroConfiguration());
    }

    @Override
    public Predicate createBooleanExpression(String expression, boolean allowQuantifiedPredicates) {
        return createBooleanExpression(expression, allowQuantifiedPredicates, getDefaultMacroConfiguration());
    }

}
