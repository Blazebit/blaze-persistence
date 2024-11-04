/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.change;

import com.blazebit.persistence.view.impl.metamodel.BasicTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;

import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SetChangeModelImpl<V> extends AbstractCollectionChangeModel<Set<V>, V> {

    public SetChangeModelImpl(ManagedViewTypeImplementor<V> type, BasicTypeImpl<V> basicType, Set<V> initial, Set<V> current, PluralDirtyChecker<Set<V>, V> pluralDirtyChecker) {
        super(type, basicType, initial, current, pluralDirtyChecker);
    }
}
