package com.blazebit.persistence.impl.expression;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ConcurrentHashMapExpressionCache implements ExpressionCache {
    private final ConcurrentMap<String, ConcurrentMap<Object, Expression>> cacheManager;

    public ConcurrentHashMapExpressionCache() {
        this.cacheManager = new ConcurrentHashMap<String, ConcurrentMap<Object, Expression>>();
    }

    @Override
    public <E extends Expression> E getOrDefault(String cacheName, String cacheKey, MacroConfiguration macroConfiguration, Supplier<E> defaultSupplier) {
        return getOrDefault(cacheName, new CacheKey(cacheKey, macroConfiguration), defaultSupplier);
    }

    @SuppressWarnings("unchecked")
    private <E extends Expression> E getOrDefault(String cacheName, Object cacheKey, Supplier<E> defaultSupplier) {
        ConcurrentMap<Object, Expression> cache = cacheManager.get(cacheName);

        if (cache == null) {
            cache = new ConcurrentHashMap<Object, Expression>();
            ConcurrentMap<Object, Expression> oldCache = cacheManager.putIfAbsent(cacheName, cache);

            if (oldCache != null) {
                cache = oldCache;
            }
        }

        E expr = (E) cache.get(cacheKey);

        if (expr == null) {
            expr = defaultSupplier.get();
            E oldExpr = (E) cache.putIfAbsent(cacheKey, expr);

            if (oldExpr != null) {
                expr = oldExpr;
            }
        }

        return (E) expr.clone();
    }

    private static final class CacheKey {
        private final String expression;
        private final MacroConfiguration macroConfiguration;

        public CacheKey(String expression, MacroConfiguration macroConfiguration) {
            this.expression = expression;
            this.macroConfiguration = macroConfiguration;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey)) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (expression != null ? !expression.equals(cacheKey.expression) : cacheKey.expression != null) return false;
            return macroConfiguration != null ? macroConfiguration.equals(cacheKey.macroConfiguration) : cacheKey.macroConfiguration == null;

        }

        @Override
        public int hashCode() {
            int result = expression != null ? expression.hashCode() : 0;
            result = 31 * result + (macroConfiguration != null ? macroConfiguration.hashCode() : 0);
            return result;
        }
    }
}
