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

import com.blazebit.persistence.parser.predicate.Predicate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractCachingExpressionFactory extends AbstractExpressionFactoryMacroAdapter {

    /**
     *
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static interface ExpressionSupplier {

        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros);
    }

    private static final ExpressionSupplier PATH_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createPathExpression(expression, macroConfiguration, usedMacros);
        }
    };

    private static final ExpressionSupplier JOIN_PATH_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createJoinPathExpression(expression, macroConfiguration, usedMacros);
        }
    };

    private static final ExpressionSupplier SIMPLE_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createSimpleExpression(expression, allowQuantifiedPredicates, macroConfiguration, usedMacros);
        }
    };

    private static final ExpressionSupplier CASE_OPERAND_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createCaseOperandExpression(expression, macroConfiguration, usedMacros);
        }
    };

    private static final ExpressionSupplier SCALAR_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createScalarExpression(expression, macroConfiguration, usedMacros);
        }
    };

    private static final ExpressionSupplier ARITHMETIC_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createArithmeticExpression(expression, macroConfiguration, usedMacros);
        }
    };

    private static final ExpressionSupplier STRING_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createStringExpression(expression, macroConfiguration, usedMacros);
        }
    };

    private static final ExpressionSupplier ORDER_BY_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createOrderByExpression(expression, macroConfiguration, usedMacros);
        }
    };

    private static final ExpressionSupplier IN_ITEM_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createInItemExpression(expression, macroConfiguration, usedMacros);
        }
    };

    private static final ExpressionSupplier IN_ITEM_OR_PATH_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createInItemOrPathExpression(expression, macroConfiguration, usedMacros);
        }
    };

    private static final ExpressionSupplier BOOLEAN_EXPRESSION_SUPPLIER = new ExpressionSupplier() {
        @Override
        public Expression get(ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
            return expressionFactory.createBooleanExpression(expression, allowQuantifiedPredicates, macroConfiguration, usedMacros);
        }
    };

    private final ExpressionFactory delegate;
    private final ExpressionCache<ExpressionCacheEntry> expressionCache;

    public AbstractCachingExpressionFactory(ExpressionFactory delegate, ExpressionCache expressionCache) {
        this.delegate = delegate;
        this.expressionCache = expressionCache;
    }

    @Override
    public <T extends ExpressionFactory> T unwrap(Class<T> clazz) {
        if (AbstractCachingExpressionFactory.class.isAssignableFrom(clazz)) {
            return (T) this;
        }
        return delegate.unwrap(clazz);
    }

    @Override
    public MacroConfiguration getDefaultMacroConfiguration() {
        return delegate.getDefaultMacroConfiguration();
    }

    @Override
    public PathExpression createPathExpression(final String expression, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return getOrDefault("com.blazebit.persistence.parser.expression.cache.PathExpression", delegate, expression, false, macroConfiguration, PATH_EXPRESSION_SUPPLIER);
    }

    @Override
    public Expression createJoinPathExpression(final String expression, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return getOrDefault("com.blazebit.persistence.parser.expression.cache.JoinPathExpression", delegate, expression, false, macroConfiguration, JOIN_PATH_EXPRESSION_SUPPLIER);
    }

    @Override
    public Expression createSimpleExpression(final String expression, final boolean allowQuantifiedPredicates, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return getOrDefault("com.blazebit.persistence.parser.expression.cache.SimpleExpression", delegate, expression, allowQuantifiedPredicates, macroConfiguration, SIMPLE_EXPRESSION_SUPPLIER);
    }

    @Override
    public Expression createCaseOperandExpression(final String expression, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return getOrDefault("com.blazebit.persistence.parser.expression.cache.CaseOperandExpression", delegate, expression, false, macroConfiguration, CASE_OPERAND_EXPRESSION_SUPPLIER);
    }

    @Override
    public Expression createScalarExpression(final String expression, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return getOrDefault("com.blazebit.persistence.parser.expression.cache.ScalarExpression", delegate, expression, false, macroConfiguration, SCALAR_EXPRESSION_SUPPLIER);
    }

    @Override
    public Expression createArithmeticExpression(final String expression, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return getOrDefault("com.blazebit.persistence.parser.expression.cache.ArithmeticExpression", delegate, expression, false, macroConfiguration, ARITHMETIC_EXPRESSION_SUPPLIER);
    }

    @Override
    public Expression createStringExpression(final String expression, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return getOrDefault("com.blazebit.persistence.parser.expression.cache.StringExpression", delegate, expression, false, macroConfiguration, STRING_EXPRESSION_SUPPLIER);
    }

    @Override
    public Expression createOrderByExpression(final String expression, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return getOrDefault("com.blazebit.persistence.parser.expression.cache.OrderByExpression", delegate, expression, false, macroConfiguration, ORDER_BY_EXPRESSION_SUPPLIER);
    }

    @Override
    public List<Expression> createInItemExpressions(final String[] parameterOrLiteralExpressions, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        if (parameterOrLiteralExpressions == null) {
            throw new NullPointerException("parameterOrLiteralExpressions");
        }
        if (parameterOrLiteralExpressions.length == 0) {
            throw new IllegalArgumentException("empty parameterOrLiteralExpressions");
        }

        List<Expression> inItemExpressions = new ArrayList<>();

        if (parameterOrLiteralExpressions.length == 1) {
            inItemExpressions.add(createInItemOrPathExpression(parameterOrLiteralExpressions[0], macroConfiguration, usedMacros));
        } else {
            for (final String parameterOrLiteralExpression : parameterOrLiteralExpressions) {
                inItemExpressions.add(createInItemExpression(parameterOrLiteralExpression, macroConfiguration, usedMacros));
            }
        }

        return inItemExpressions;
    }

    @Override
    public Expression createInItemExpression(final String parameterOrLiteralExpression, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return getOrDefault("com.blazebit.persistence.parser.expression.cache.InPredicateExpression", delegate, parameterOrLiteralExpression, false, macroConfiguration, IN_ITEM_EXPRESSION_SUPPLIER);
    }

    @Override
    public Expression createInItemOrPathExpression(final String parameterOrLiteralExpression, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return getOrDefault("com.blazebit.persistence.parser.expression.cache.InPredicateSingleExpression", delegate, parameterOrLiteralExpression, false, macroConfiguration, IN_ITEM_OR_PATH_EXPRESSION_SUPPLIER);
    }

    @Override
    public Predicate createBooleanExpression(final String expression, final boolean allowQuantifiedPredicates, final MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return getOrDefault("com.blazebit.persistence.parser.expression.cache.PredicateExpression", delegate, expression, allowQuantifiedPredicates, macroConfiguration, BOOLEAN_EXPRESSION_SUPPLIER);
    }

    private <E extends Expression> E getOrDefault(String cacheName, ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, ExpressionSupplier defaultExpressionSupplier) {
        // Find the expression cache entry
        ExpressionCacheEntry exprEntry = expressionCache.get(cacheName, expression);
        MacroConfiguration macroKey = null;
        Expression expr;

        if (exprEntry == null) {
            // Create the expression object
            Set<String> usedMacros = new HashSet<>();
            expr = defaultExpressionSupplier.get(expressionFactory, expression, allowQuantifiedPredicates, macroConfiguration, usedMacros);
            // The cache entry is macro aware
            exprEntry = new ExpressionCacheEntry(expr, usedMacros);
            if (!usedMacros.isEmpty()) {
                macroKey = exprEntry.createKey(macroConfiguration);
                // Macro key is null when one macro reports it is non-cacheable
                if (macroKey == null) {
                    // Since it's not cacheable, there is no need to clone the value
                    return (E) expr;
                }
                exprEntry.addMacroConfigurationExpression(macroKey, expr);
            }

            expressionCache.putIfAbsent(cacheName, expression, exprEntry);
            return (E) expr.clone(false);
        }

        // Fast-path if macro-free
        if (exprEntry.usedMacros == null) {
            expr = exprEntry.expression;
        } else {
            // Find a macro-aware entry
            if (macroKey == null) {
                macroKey = exprEntry.createKey(macroConfiguration);
                // Macro key is null when one macro reports it is non-cacheable, which can totally happen here
                if (macroKey == null) {
                    return (E) defaultExpressionSupplier.get(expressionFactory, expression, allowQuantifiedPredicates, macroConfiguration, null);
                }
            }
            expr = exprEntry.macroConfigurationCache.get(macroKey);

            // Create the macro-aware expression object
            if (expr == null) {
                expr = defaultExpressionSupplier.get(expressionFactory, expression, allowQuantifiedPredicates, macroConfiguration, null);
                Expression oldExpr = exprEntry.macroConfigurationCache.putIfAbsent(macroKey, expr);

                if (oldExpr != null) {
                    expr = oldExpr;
                }
            }
        }

        return (E) expr.clone(false);
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static final class ExpressionCacheEntry {
        final Expression expression;
        final Set<String> usedMacros;
        final ConcurrentHashMap<MacroConfiguration, Expression> macroConfigurationCache;

        public ExpressionCacheEntry(Expression expression, Set<String> usedMacros) {
            if (usedMacros.isEmpty()) {
                // The expression in the entry is just the fast path for the macro-free case
                // An expression that didn't resolve macros is always macro-free, regardless of possible later registrations
                this.expression = expression;
                this.usedMacros = null;
                this.macroConfigurationCache = null;
            } else {
                this.expression = null;
                this.usedMacros = usedMacros;
                this.macroConfigurationCache = new ConcurrentHashMap<>();
            }
        }

        public MacroConfiguration createKey(MacroConfiguration macroConfiguration) {
            Map<String, MacroFunction> macros = new HashMap<>(usedMacros.size());
            for (String usedMacro : usedMacros) {
                MacroFunction macroFunction = macroConfiguration.get(usedMacro);
                if (!macroFunction.supportsCaching()) {
                    return null;
                }
                macros.put(usedMacro, macroFunction);
            }
            return MacroConfiguration.of(macros);
        }

        public void addMacroConfigurationExpression(MacroConfiguration macroConfiguration, Expression expression) {
            macroConfigurationCache.put(macroConfiguration, expression);
        }
    }
}
