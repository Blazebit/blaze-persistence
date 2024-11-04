/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SortedSetFactory implements PluralObjectFactory<NavigableSet<?>> {

    public static final SortedSetFactory INSTANCE = new SortedSetFactory(null);

    private final Comparator<Object> comparator;

    public SortedSetFactory(Comparator<Object> comparator) {
        this.comparator = comparator;
    }

    @Override
    public NavigableSet<?> createCollection(int size) {
        return new TreeSet<>(comparator);
    }

}
