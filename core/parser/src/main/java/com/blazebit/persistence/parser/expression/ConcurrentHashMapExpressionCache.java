/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
