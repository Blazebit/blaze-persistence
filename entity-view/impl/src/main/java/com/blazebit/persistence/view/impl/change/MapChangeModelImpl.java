/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.change;

import com.blazebit.persistence.view.impl.metamodel.BasicTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;

import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapChangeModelImpl<K, V> extends AbstractMapChangeModel<K, V> {

    public MapChangeModelImpl(ManagedViewTypeImplementor<K> keyType, BasicTypeImpl<K> keyBasicType, ManagedViewTypeImplementor<V> type, BasicTypeImpl<V> basicType, Map<K, V> initial, Map<K, V> current, MapDirtyChecker<Map<K, V>, K, V> pluralDirtyChecker) {
        super(keyType, keyBasicType, type, basicType, initial, current, pluralDirtyChecker);
    }
}
