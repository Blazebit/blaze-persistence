/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.keyset;

import java.util.HashMap;
import java.util.Map;

import com.blazebit.persistence.KeysetBuilder;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class KeysetBuilderImpl<T> implements KeysetBuilder<T> {

    private final Map<String, Object> keysetValues;
    private final T result;
    private final KeysetBuilderEndedListener listener;
    private final KeysetMode mode;

    public KeysetBuilderImpl(T result, KeysetBuilderEndedListener listener, KeysetMode mode) {
        this.keysetValues = new HashMap<String, Object>();
        this.result = result;
        this.listener = listener;
        this.mode = mode;
    }

    @Override
    public KeysetBuilder<T> with(String expression, Object value) {
        keysetValues.put(expression, value);
        return this;
    }

    @Override
    public T end() {
        listener.onBuilderEnded(this);
        return result;
    }

    public Map<String, Object> getKeysetValues() {
        return keysetValues;
    }

    public KeysetMode getMode() {
        return mode;
    }
}
