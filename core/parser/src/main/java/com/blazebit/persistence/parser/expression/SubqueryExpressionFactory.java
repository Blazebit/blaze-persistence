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

import com.blazebit.persistence.parser.JPQLSelectExpressionParser;
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

    private final ExpressionFactory delegate;

    private final RuleInvoker simpleExpressionRuleInvoker = new RuleInvoker() {

        @Override
        public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
            return parser.parseSimpleSubqueryExpression();
        }
    };

    public SubqueryExpressionFactory(Set<String> aggregateFunctions, Map<String, Class<?>> entityTypes, Map<String, Class<Enum<?>>> enumTypes, boolean allowTreatJoinExtension, boolean optimize, ExpressionFactory delegate) {
        super(aggregateFunctions, entityTypes, enumTypes, allowTreatJoinExtension, optimize);
        this.delegate = delegate;
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration) {
        return super.createSimpleExpression(expression, allowQuantifiedPredicates, macroConfiguration);
    }

    @Override
    protected RuleInvoker getSimpleExpressionRuleInvoker() {
        return simpleExpressionRuleInvoker;
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
    public Expression createCaseOperandExpression(String caseOperandExpression) {
        return delegate.createCaseOperandExpression(caseOperandExpression);
    }

    @Override
    public Expression createCaseOperandExpression(String caseOperandExpression, MacroConfiguration macroConfiguration) {
        return delegate.createCaseOperandExpression(caseOperandExpression, macroConfiguration);
    }

    @Override
    public Expression createScalarExpression(String expression) {
        return delegate.createScalarExpression(expression);
    }

    @Override
    public Expression createScalarExpression(String expression, MacroConfiguration macroConfiguration) {
        return delegate.createScalarExpression(expression, macroConfiguration);
    }

    @Override
    public Expression createArithmeticExpression(String expression) {
        return delegate.createArithmeticExpression(expression);
    }

    @Override
    public Expression createArithmeticExpression(String expression, MacroConfiguration macroConfiguration) {
        return delegate.createArithmeticExpression(expression, macroConfiguration);
    }

    @Override
    public Expression createStringExpression(String expression) {
        return delegate.createStringExpression(expression);
    }

    @Override
    public Expression createStringExpression(String expression, MacroConfiguration macroConfiguration) {
        return delegate.createStringExpression(expression, macroConfiguration);
    }

    @Override
    public Expression createOrderByExpression(String expression) {
        return delegate.createOrderByExpression(expression);
    }

    @Override
    public Expression createOrderByExpression(String expression, MacroConfiguration macroConfiguration) {
        return delegate.createOrderByExpression(expression, macroConfiguration);
    }

    @Override
    public List<Expression> createInItemExpressions(String[] parameterOrLiteralExpressions) {
        return delegate.createInItemExpressions(parameterOrLiteralExpressions);
    }

    @Override
    public List<Expression> createInItemExpressions(String[] parameterOrLiteralExpressions, MacroConfiguration macroConfiguration) {
        return delegate.createInItemExpressions(parameterOrLiteralExpressions, macroConfiguration);
    }

    @Override
    public Expression createInItemExpression(String parameterOrLiteralExpression) {
        return delegate.createInItemExpression(parameterOrLiteralExpression);
    }

    @Override
    public Expression createInItemExpression(String parameterOrLiteralExpression, MacroConfiguration macroConfiguration) {
        return delegate.createInItemExpression(parameterOrLiteralExpression, macroConfiguration);
    }

    @Override
    public Expression createInItemOrPathExpression(String parameterOrLiteralExpression, MacroConfiguration macroConfiguration) {
        return delegate.createInItemOrPathExpression(parameterOrLiteralExpression, macroConfiguration);
    }

    @Override
    public Predicate createBooleanExpression(String expression, boolean allowQuantifiedPredicates) {
        return delegate.createBooleanExpression(expression, allowQuantifiedPredicates);
    }

    @Override
    public Predicate createBooleanExpression(String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration) {
        return delegate.createBooleanExpression(expression, allowQuantifiedPredicates, macroConfiguration);
    }

    @Override
    public PathExpression createPathExpression(String expression) {
        return delegate.createPathExpression(expression);
    }

    @Override
    public PathExpression createPathExpression(String expression, MacroConfiguration macroConfiguration) {
        return delegate.createPathExpression(expression, macroConfiguration);
    }

    @Override
    public Expression createJoinPathExpression(String expression) {
        return delegate.createJoinPathExpression(expression);
    }

    @Override
    public Expression createJoinPathExpression(String expression, MacroConfiguration macroConfiguration) {
        return delegate.createJoinPathExpression(expression, macroConfiguration);
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowQuantifiedPredicates) {
        return delegate.createSimpleExpression(expression, allowQuantifiedPredicates);
    }
}
