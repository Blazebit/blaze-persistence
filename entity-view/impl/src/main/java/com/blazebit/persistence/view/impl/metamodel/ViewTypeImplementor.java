/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.metamodel.ViewType;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ViewTypeImplementor<X> extends ViewType<X>, ManagedViewTypeImplementor<X> {

    ViewTypeImplementor<X> getRealType();

    boolean supportsUserTypeEquals();
}
