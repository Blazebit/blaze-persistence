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
public class ImmutableListChangeModel<E> extends AbstractImmutablePluralChangeModel<List<E>, E> {

    public ImmutableListChangeModel(ManagedViewTypeImplementor<E> type, BasicTypeImpl<E> basicType, List<E> initial, List<E> current) {
        super(type, basicType, initial, current);
    }
}
