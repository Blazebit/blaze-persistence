/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class OrderedMapUserTypeWrapper<K, V> extends AbstractMapUserTypeWrapper<Map<K, V>, K, V> {

    public OrderedMapUserTypeWrapper(BasicUserType<K> keyUserType, BasicUserType<V> elementUserType) {
        super(keyUserType, elementUserType);
    }

    @Override
    protected Map<K, V> createCollection(int size) {
        return new LinkedHashMap<>(size);
    }
}
