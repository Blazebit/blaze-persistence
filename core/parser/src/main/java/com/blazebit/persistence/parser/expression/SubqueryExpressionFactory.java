/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

import com.blazebit.persistence.parser.FunctionKind;
import com.blazebit.persistence.parser.JPQLNextParser;
import com.blazebit.persistence.parser.predicate.Predicate;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class SubqueryExpressionFactory extends AbstractExpressionFactory {

    private static final RuleInvoker SIMPLE_EXPRESSION_RULE_INVOKER = new RuleInvoker() {

        @Override
        public ParserRuleContext invokeRule(JPQLNextParser parser) {
            return parser.parseExpression();
        }
    };

    private final ExpressionFactory delegate;

    public SubqueryExpressionFactory(Map<String, FunctionKind> functions, Map<String, Class<?>> entityTypes, Map<String, Class<Enum<?>>> enumTypes, Map<String, Class<Enum<?>>> enumTypesForLiterals, boolean allowTreatJoinExtension, boolean optimize, ExpressionFactory delegate) {
        super(functions, entityTypes, enumTypes, enumTypesForLiterals, optimize);
        this.delegate = delegate;
    }

    @Override
    protected RuleInvoker getSimpleExpressionRuleInvoker() {
        return SIMPLE_EXPRESSION_RULE_INVOKER;
    }

    @Override
    public <T extends ExpressionFactory> T unwrap(Class<T> clazz) {
        if (SubqueryExpressionFactory.class.isAssignableFrom(clazz)) {
            return (T) this;
        }
        return delegate.unwrap(clazz);
    }

    // Delegates

    @Override
    public Expression createSimpleExpression(String expression) {
        return delegate.createSimpleExpression(expression);
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowQuantifiedPredicates) {
        return delegate.createSimpleExpression(expression, true, allowQuantifiedPredicates);
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowOuter, boolean allowQuantifiedPredicates) {
        return delegate.createSimpleExpression(expression, true, allowQuantifiedPredicates);
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowOuter, boolean allowQuantifiedPredicates, boolean allowObjectExpression) {
        return delegate.createSimpleExpression(expression, allowOuter, allowQuantifiedPredicates, allowObjectExpression);
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowOuter, boolean allowQuantifiedPredicates, boolean allowObjectExpression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return super.createSimpleExpression(expression, true, allowQuantifiedPredicates, allowObjectExpression, macroConfiguration, usedMacros);
    }

    @Override
    public List<Expression> createInItemExpressions(String[] parameterOrLiteralExpressions) {
        return delegate.createInItemExpressions(parameterOrLiteralExpressions);
    }

    @Override
    public List<Expression> createInItemExpressions(String[] parameterOrLiteralExpressions, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return delegate.createInItemExpressions(parameterOrLiteralExpressions, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createInItemExpression(String parameterOrLiteralExpression) {
        return delegate.createInItemExpression(parameterOrLiteralExpression);
    }

    @Override
    public Expression createInItemExpression(String parameterOrLiteralExpression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return delegate.createInItemExpression(parameterOrLiteralExpression, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createInItemOrPathExpression(String parameterOrLiteralExpression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return delegate.createInItemOrPathExpression(parameterOrLiteralExpression, macroConfiguration, usedMacros);
    }

    @Override
    public Predicate createBooleanExpression(String expression, boolean allowQuantifiedPredicates) {
        return delegate.createBooleanExpression(expression, allowQuantifiedPredicates);
    }

    @Override
    public Predicate createBooleanExpression(String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return delegate.createBooleanExpression(expression, allowQuantifiedPredicates, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createJoinPathExpression(String expression) {
        return delegate.createJoinPathExpression(expression);
    }

    @Override
    public Expression createJoinPathExpression(String expression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return delegate.createJoinPathExpression(expression, macroConfiguration, usedMacros);
    }

    @Override
    public PathExpression createPathExpression(String expression) {
        return delegate.createPathExpression(expression);
    }

    @Override
    public Expression createPathExpression(String expression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return delegate.createPathExpression(expression, macroConfiguration, usedMacros);
    }
}
