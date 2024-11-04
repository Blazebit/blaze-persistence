/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class OrderedSetUserTypeWrapper<V> extends AbstractCollectionUserTypeWrapper<Set<V>, V> {

    public OrderedSetUserTypeWrapper(BasicUserType<V> elementUserType) {
        super(elementUserType);
    }

    @Override
    protected Set<V> createCollection(int size) {
        return new LinkedHashSet<>(size);
    }
}
