package com.blazebit.persistence.impl.expression;

import com.blazebit.persistence.impl.predicate.Predicate;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractExpressionFactoryAdapter implements ExpressionFactory {

    @Override
    public PathExpression createPathExpression(String expression) {
        return createPathExpression(expression, null);
    }

    @Override
    public Expression createJoinPathExpression(String expression) {
        return createJoinPathExpression(expression, null);
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowQuantifiedPredicates) {
        return createSimpleExpression(expression, allowQuantifiedPredicates, null);
    }

    @Override
    public Expression createCaseOperandExpression(String caseOperandExpression) {
        return createCaseOperandExpression(caseOperandExpression, null);
    }

    @Override
    public Expression createScalarExpression(String expression) {
        return createScalarExpression(expression, null);
    }

    @Override
    public Expression createArithmeticExpression(String expression) {
        return createArithmeticExpression(expression, null);
    }

    @Override
    public Expression createStringExpression(String expression) {
        return createStringExpression(expression, null);
    }

    @Override
    public Expression createOrderByExpression(String expression) {
        return createOrderByExpression(expression, null);
    }

    @Override
    public List<Expression> createInItemExpressions(String[] parameterOrLiteralExpressions) {
        return createInItemExpressions(parameterOrLiteralExpressions, null);
    }

    @Override
    public Expression createInItemExpression(String parameterOrLiteralExpression) {
        return createInItemExpression(parameterOrLiteralExpression, null);
    }

    @Override
    public Predicate createBooleanExpression(String expression, boolean allowQuantifiedPredicates) {
        return createBooleanExpression(expression, allowQuantifiedPredicates, null);
    }

}
