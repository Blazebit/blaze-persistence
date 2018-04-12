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

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ExpressionCache {

    public static final ExpressionSupplier PATH_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createPathExpression(expression, macroConfiguration, usedMacros);
        }
    };

    public static final ExpressionSupplier JOIN_PATH_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createJoinPathExpression(expression, macroConfiguration, usedMacros);
        }
    };

    public static final ExpressionSupplier SIMPLE_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createSimpleExpression(expression, allowQuantifiedPredicates, macroConfiguration, usedMacros);
        }
    };

    public static final ExpressionSupplier CASE_OPERAND_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createCaseOperandExpression(expression, macroConfiguration, usedMacros);
        }
    };

    public static final ExpressionSupplier SCALAR_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createScalarExpression(expression, macroConfiguration, usedMacros);
        }
    };

    public static final ExpressionSupplier ARITHMETIC_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createArithmeticExpression(expression, macroConfiguration, usedMacros);
        }
    };

    public static final ExpressionSupplier STRING_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createStringExpression(expression, macroConfiguration, usedMacros);
        }
    };

    public static final ExpressionSupplier ORDER_BY_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createOrderByExpression(expression, macroConfiguration, usedMacros);
        }
    };

    public static final ExpressionSupplier IN_ITEM_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createInItemExpression(expression, macroConfiguration, usedMacros);
        }
    };

    public static final ExpressionSupplier IN_ITEM_OR_PATH_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createInItemOrPathExpression(expression, macroConfiguration, usedMacros);
        }
    };

    public static final ExpressionSupplier BOOLEAN_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createBooleanExpression(expression, allowQuantifiedPredicates, macroConfiguration, usedMacros);
        }
    };

    public <E extends Expression> E getOrDefault(String cacheName, ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, ExpressionSupplier defaultExpressionSupplier);

    /**
     *
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static interface ExpressionSupplier {

        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros);
    }
}
