/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

import com.blazebit.persistence.parser.FunctionKind;
import com.blazebit.persistence.parser.JPQLNextParser;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collections;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public final class ExpressionFactoryImpl extends AbstractExpressionFactory {

    private static final RuleInvoker SIMPLE_EXPRESSION_RULE_INVOKER = new RuleInvoker() {

        @Override
        public ParserRuleContext invokeRule(JPQLNextParser parser) {
            return parser.parseExpression();
        }
    };

    public ExpressionFactoryImpl(Map<String, FunctionKind> functions, boolean allowTreatJoinExtension, boolean optimize) {
        this(functions, Collections.EMPTY_MAP, Collections.EMPTY_MAP, Collections.EMPTY_MAP, allowTreatJoinExtension, optimize);
    }

    public ExpressionFactoryImpl(Map<String, FunctionKind> functions, Map<String, Class<?>> entityTypes, Map<String, Class<Enum<?>>> enumTypes, Map<String, Class<Enum<?>>> enumTypesForLiterals, boolean allowTreatJoinExtension, boolean optimize) {
        super(functions, entityTypes, enumTypes, enumTypesForLiterals, optimize);
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
