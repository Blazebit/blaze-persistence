/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SortedMapUserTypeWrapper<K, V> extends AbstractMapUserTypeWrapper<Map<K, V>, K, V> {

    private final Comparator<K> comparator;

    public SortedMapUserTypeWrapper(BasicUserType<K> keyUserType, BasicUserType<V> elementUserType, Comparator<K> comparator) {
        super(keyUserType, elementUserType);
        this.comparator = comparator;
    }

    @Override
    protected Map<K, V> createCollection(int size) {
        return new TreeMap<>(comparator);
    }
}
