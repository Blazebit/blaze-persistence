/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.change;

import com.blazebit.persistence.view.impl.metamodel.BasicTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;

import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ListChangeModelImpl<V> extends AbstractCollectionChangeModel<List<V>, V> {

    public ListChangeModelImpl(ManagedViewTypeImplementor<V> type, BasicTypeImpl<V> basicType, List<V> initial, List<V> current, PluralDirtyChecker<List<V>, V> pluralDirtyChecker) {
        super(type, basicType, initial, current, pluralDirtyChecker);
    }
}
