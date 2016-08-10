/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.impl.expression;

import java.util.List;
import java.util.Set;

import com.blazebit.persistence.parser.JPQLSelectExpressionParser;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class SubqueryExpressionFactory extends AbstractExpressionFactory {

    private final ExpressionFactory delegate;

    private final RuleInvoker simpleExpressionRuleInvoker = new RuleInvoker() {

        @Override
        public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
            return parser.parseSimpleSubqueryExpression();
        }
    };

    public SubqueryExpressionFactory(Set<String> aggregateFunctions, ExpressionFactory delegate) {
        super(aggregateFunctions);
        this.delegate = delegate;
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowQuantifiedPredicates) {
        return super.createSimpleExpression(expression, allowQuantifiedPredicates);
    }

    @Override
    protected RuleInvoker getSimpleExpressionRuleInvoker() {
        return simpleExpressionRuleInvoker;
    }
    
    // Delegates

    @Override
    public PathExpression createPathExpression(String expression) {
        return delegate.createPathExpression(expression);
    }

    @Override
    public Expression createCaseOperandExpression(String caseOperandExpression) {
        return delegate.createCaseOperandExpression(caseOperandExpression);
    }

    @Override
    public Expression createScalarExpression(String expression) {
        return delegate.createScalarExpression(expression);
    }

    @Override
    public Expression createArithmeticExpression(String expression) {
        return delegate.createArithmeticExpression(expression);
    }

    @Override
    public Expression createStringExpression(String expression) {
        return delegate.createStringExpression(expression);
    }

    @Override
    public Expression createOrderByExpression(String expression) {
        return delegate.createOrderByExpression(expression);
    }

    @Override
    public List<Expression> createInItemExpressions(String[] parameterOrLiteralExpressions) {
        return delegate.createInItemExpressions(parameterOrLiteralExpressions);
    }

}
