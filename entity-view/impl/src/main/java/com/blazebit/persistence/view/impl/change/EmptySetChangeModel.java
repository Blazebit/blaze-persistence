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
public class EmptySetChangeModel<E> extends AbstractEmptyPluralChangeModel<Set<E>, E> {

    public EmptySetChangeModel(ManagedViewTypeImplementor<E> type, BasicTypeImpl<E> basicType) {
        super(type, basicType);
    }

}
