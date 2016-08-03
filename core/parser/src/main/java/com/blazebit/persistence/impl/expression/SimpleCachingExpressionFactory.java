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

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author cpbec
 */
public class SimpleCachingExpressionFactory extends AbstractCachingExpressionFactory {

    private final ConcurrentMap<String, ConcurrentMap<Object, Expression>> cacheManager;

    public SimpleCachingExpressionFactory(ExpressionFactory delegate) {
        super(delegate);
        this.cacheManager = new ConcurrentHashMap<String, ConcurrentMap<Object, Expression>>();
    }

    @Override
    protected <E extends Expression> E getOrDefault(String cacheName, String cacheKey, Supplier<E> defaultSupplier) {
        return getOrDefault(cacheName, (Object) cacheKey, defaultSupplier);
    }

    @Override
    protected <E extends Expression> E getOrDefault(String cacheName, Object[] cacheKey, Supplier<E> defaultSupplier) {
        return getOrDefault(cacheName, (Object) new ObjectArrayCacheKey(cacheKey), defaultSupplier);
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
    
    private static final class ObjectArrayCacheKey {
        private final Object[] value;

        public ObjectArrayCacheKey(Object[] value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(value);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ObjectArrayCacheKey other = (ObjectArrayCacheKey) obj;
            if (!Arrays.equals(value, other.value))
                return false;
            return true;
        }
    }

}
