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
public class EmptyListChangeModel<E> extends AbstractEmptyPluralChangeModel<List<E>, E> {

    public EmptyListChangeModel(ManagedViewTypeImplementor<E> type, BasicTypeImpl<E> basicType) {
        super(type, basicType);
    }

}
