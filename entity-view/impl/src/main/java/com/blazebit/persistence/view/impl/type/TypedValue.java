/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class TypedValue<V> {

    private final BasicUserType<V> type;
    private V value;

    public TypedValue(BasicUserType<V> type) {
        this.type = type;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        return type.isEqual(value, (V) o);
    }

    @Override
    public int hashCode() {
        return type.hashCode(value);
    }
}
