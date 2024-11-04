/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.metamodel.FlatViewType;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface FlatViewTypeImplementor<X> extends FlatViewType<X>, ManagedViewTypeImplementor<X> {

    public FlatViewTypeImplementor<X> getRealType();
}
