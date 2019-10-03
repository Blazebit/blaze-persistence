/*
 * Copyright 2014 - 2019 Blazebit.
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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ConcurrentHashMapExpressionCache<T> implements ExpressionCache<T> {

    private final ConcurrentMap<String, ConcurrentMap<Key, T>> cacheManager;

    public ConcurrentHashMapExpressionCache() {
        this.cacheManager = new ConcurrentHashMap<>();
    }

    @Override
    public T get(String cacheName, Key key) {
        final ConcurrentMap<Key, T> cache = cacheManager.get(cacheName);
        return cache == null ? null : cache.get(key);
    }

    @Override
    public T putIfAbsent(String cacheName, Key key, T value) {
        // Find the cache manager
        ConcurrentMap<Key, T> cache = cacheManager.get(cacheName);

        if (cache == null) {
            cache = new ConcurrentHashMap<>();
            ConcurrentMap<Key, T> oldCache = cacheManager.putIfAbsent(cacheName, cache);

            if (oldCache != null) {
                cache = oldCache;
            }
        }

        T oldValue = cache.putIfAbsent(key, value);
        if (oldValue != null) {
            return oldValue;
        }

        return value;
    }
}
