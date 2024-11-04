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
public class EmptyCollectionChangeModel<E> extends AbstractEmptyPluralChangeModel<Collection<E>, E> {

    public EmptyCollectionChangeModel(ManagedViewTypeImplementor<E> type, BasicTypeImpl<E> basicType) {
        super(type, basicType);
    }

}
