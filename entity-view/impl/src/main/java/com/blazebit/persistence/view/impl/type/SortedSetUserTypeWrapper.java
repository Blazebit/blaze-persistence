/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SortedSetUserTypeWrapper<V> extends AbstractCollectionUserTypeWrapper<SortedSet<V>, V> {

    private final Comparator<V> comparator;

    public SortedSetUserTypeWrapper(BasicUserType<V> elementUserType, Comparator<V> comparator) {
        super(elementUserType);
        this.comparator = comparator;
    }

    @Override
    protected SortedSet<V> createCollection(int size) {
        return new TreeSet<>(comparator);
    }
}
