/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class NormalSetUserTypeWrapper<V> extends AbstractCollectionUserTypeWrapper<Set<V>, V> {

    public NormalSetUserTypeWrapper(BasicUserType<V> elementUserType) {
        super(elementUserType);
    }

    @Override
    protected Set<V> createCollection(int size) {
        return new HashSet<>(size);
    }
}
