/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.change;

import com.blazebit.persistence.view.impl.metamodel.BasicTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;

import java.util.Collection;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CollectionChangeModelImpl<V> extends AbstractCollectionChangeModel<Collection<V>, V> {

    public CollectionChangeModelImpl(ManagedViewTypeImplementor<V> type, BasicTypeImpl<V> basicType, Collection<V> initial, Collection<V> current, PluralDirtyChecker<Collection<V>, V> pluralDirtyChecker) {
        super(type, basicType, initial, current, pluralDirtyChecker);
    }
}
