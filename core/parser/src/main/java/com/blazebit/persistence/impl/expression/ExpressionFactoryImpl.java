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

import java.util.Set;

import com.blazebit.persistence.parser.JPQLSelectExpressionParser;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public final class ExpressionFactoryImpl extends AbstractExpressionFactory {

    private final RuleInvoker simpleExpressionRuleInvoker = new RuleInvoker() {

        @Override
        public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
            return parser.parseSimpleExpression();
        }
    };

    public ExpressionFactoryImpl(Set<String> aggregateFunctions) {
        super(aggregateFunctions);
    }

    @Override
    protected RuleInvoker getSimpleExpressionRuleInvoker() {
        return simpleExpressionRuleInvoker;
    }

}
