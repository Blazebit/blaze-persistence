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
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public final class ExpressionFactoryImpl extends AbstractExpressionFactory {

    private static final RuleInvoker SIMPLE_EXPRESSION_RULE_INVOKER = new RuleInvoker() {

        @Override
        public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
            return parser.parseSimpleExpression();
        }
    };

    public ExpressionFactoryImpl(Set<String> aggregateFunctions, boolean allowTreatJoinExtension, boolean optimize) {
        this(aggregateFunctions, Collections.EMPTY_MAP, Collections.EMPTY_MAP, allowTreatJoinExtension, optimize);
    }

    public ExpressionFactoryImpl(Set<String> aggregateFunctions, Map<String, Class<?>> entityTypes, Map<String, Class<Enum<?>>> enumTypes, boolean allowTreatJoinExtension, boolean optimize) {
        super(aggregateFunctions, entityTypes, enumTypes, allowTreatJoinExtension, optimize);
    }

    @Override
    protected RuleInvoker getSimpleExpressionRuleInvoker() {
        return SIMPLE_EXPRESSION_RULE_INVOKER;
    }

    @Override
    public <T extends ExpressionFactory> T unwrap(Class<T> clazz) {
        if (clazz.isAssignableFrom(ExpressionFactoryImpl.class)) {
            return (T) this;
        }
        return null;
    }
}
