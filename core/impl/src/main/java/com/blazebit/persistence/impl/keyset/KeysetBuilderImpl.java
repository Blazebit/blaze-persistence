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
