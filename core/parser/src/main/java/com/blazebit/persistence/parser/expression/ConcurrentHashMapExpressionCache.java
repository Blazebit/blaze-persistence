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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ConcurrentHashMapExpressionCache implements ExpressionCache {
    private final ConcurrentMap<String, ConcurrentMap<String, ExpressionCacheEntry>> cacheManager;

    public ConcurrentHashMapExpressionCache() {
        this.cacheManager = new ConcurrentHashMap<>();
    }

    @Override
    public <E extends Expression> E getOrDefault(String cacheName, ExpressionFactory expressionFactory, String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, ExpressionSupplier defaultExpressionSupplier) {
        // Find the cache manager
        ConcurrentMap<String, ExpressionCacheEntry> cache = cacheManager.get(cacheName);

        if (cache == null) {
            cache = new ConcurrentHashMap<>();
            ConcurrentMap<String, ExpressionCacheEntry> oldCache = cacheManager.putIfAbsent(cacheName, cache);

            if (oldCache != null) {
                cache = oldCache;
            }
        }

        // Find the expression cache entry
        ExpressionCacheEntry exprEntry = cache.get(expression);
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
                    return (E) expr;
                }
                exprEntry.addMacroConfigurationExpression(macroKey, expr);
            }

            cache.putIfAbsent(expression, exprEntry);
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
    public static final class ExpressionCacheEntry {
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
