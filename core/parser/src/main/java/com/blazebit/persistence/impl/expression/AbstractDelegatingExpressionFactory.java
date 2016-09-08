package com.blazebit.persistence.impl.expression;

import com.blazebit.persistence.impl.predicate.Predicate;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractDelegatingExpressionFactory implements ExpressionFactory {

    protected abstract ExpressionFactory getDelegate();

    @Override
    public <T extends ExpressionFactory> T unwrap(Class<T> clazz) {
        return getDelegate().unwrap(clazz);
    }

    @Override
    public PathExpression createPathExpression(String expression) {
        return getDelegate().createPathExpression(expression);
    }

    @Override
    public PathExpression createPathExpression(String expression, MacroConfiguration macroConfiguration) {
        return getDelegate().createPathExpression(expression, macroConfiguration);
    }

    @Override
    public Expression createJoinPathExpression(String expression) {
        return getDelegate().createJoinPathExpression(expression);
    }

    @Override
    public Expression createJoinPathExpression(String expression, MacroConfiguration macroConfiguration) {
        return getDelegate().createJoinPathExpression(expression, macroConfiguration);
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowQuantifiedPredicates) {
        return getDelegate().createSimpleExpression(expression, allowQuantifiedPredicates);
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration) {
        return getDelegate().createSimpleExpression(expression, allowQuantifiedPredicates, macroConfiguration);
    }

    @Override
    public Expression createCaseOperandExpression(String caseOperandExpression) {
        return getDelegate().createCaseOperandExpression(caseOperandExpression);
    }

    @Override
    public Expression createCaseOperandExpression(String caseOperandExpression, MacroConfiguration macroConfiguration) {
        return getDelegate().createCaseOperandExpression(caseOperandExpression, macroConfiguration);
    }

    @Override
    public Expression createScalarExpression(String expression) {
        return getDelegate().createScalarExpression(expression);
    }

    @Override
    public Expression createScalarExpression(String expression, MacroConfiguration macroConfiguration) {
        return getDelegate().createScalarExpression(expression, macroConfiguration);
    }

    @Override
    public Expression createArithmeticExpression(String expression) {
        return getDelegate().createArithmeticExpression(expression);
    }

    @Override
    public Expression createArithmeticExpression(String expression, MacroConfiguration macroConfiguration) {
        return getDelegate().createArithmeticExpression(expression, macroConfiguration);
    }

    @Override
    public Expression createStringExpression(String expression) {
        return getDelegate().createStringExpression(expression);
    }

    @Override
    public Expression createStringExpression(String expression, MacroConfiguration macroConfiguration) {
        return getDelegate().createStringExpression(expression, macroConfiguration);
    }

    @Override
    public Expression createOrderByExpression(String expression) {
        return getDelegate().createOrderByExpression(expression);
    }

    @Override
    public Expression createOrderByExpression(String expression, MacroConfiguration macroConfiguration) {
        return getDelegate().createOrderByExpression(expression, macroConfiguration);
    }

    @Override
    public List<Expression> createInItemExpressions(String[] parameterOrLiteralExpressions) {
        return getDelegate().createInItemExpressions(parameterOrLiteralExpressions);
    }

    @Override
    public List<Expression> createInItemExpressions(String[] parameterOrLiteralExpressions, MacroConfiguration macroConfiguration) {
        return getDelegate().createInItemExpressions(parameterOrLiteralExpressions, macroConfiguration);
    }

    @Override
    public Expression createInItemExpression(String parameterOrLiteralExpression) {
        return getDelegate().createInItemExpression(parameterOrLiteralExpression);
    }

    @Override
    public Expression createInItemExpression(String parameterOrLiteralExpression, MacroConfiguration macroConfiguration) {
        return getDelegate().createInItemExpression(parameterOrLiteralExpression, macroConfiguration);
    }

    @Override
    public Predicate createBooleanExpression(String expression, boolean allowQuantifiedPredicates) {
        return getDelegate().createBooleanExpression(expression, allowQuantifiedPredicates);
    }

    @Override
    public Predicate createBooleanExpression(String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration) {
        return getDelegate().createBooleanExpression(expression, allowQuantifiedPredicates, macroConfiguration);
    }
}
