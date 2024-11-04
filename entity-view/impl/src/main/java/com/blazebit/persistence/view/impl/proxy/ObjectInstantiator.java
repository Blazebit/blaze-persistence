/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.proxy;

/**
 *
 * @author Christian Beikov
 * @since 1.0.6
 */
public interface ObjectInstantiator<T> {

    public T newInstance(Object[] tuple);
}
