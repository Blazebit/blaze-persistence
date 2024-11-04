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
public class ImmutableSetChangeModel<E> extends AbstractImmutablePluralChangeModel<Set<E>, E> {

    public ImmutableSetChangeModel(ManagedViewTypeImplementor<E> type, BasicTypeImpl<E> basicType, Set<E> initial, Set<E> current) {
        super(type, basicType, initial, current);
    }
}
