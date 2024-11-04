/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface PluralObjectFactory<C> {

    public C createCollection(int size);

}
