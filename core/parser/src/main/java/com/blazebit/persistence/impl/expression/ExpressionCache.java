package com.blazebit.persistence.impl.expression;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ExpressionCache {

    public static interface Supplier<T> {

        public T get();
    }

    public <E extends Expression> E getOrDefault(String cacheName, String cacheKey, MacroConfiguration macroConfiguration, Supplier<E> defaultSupplier);
}
