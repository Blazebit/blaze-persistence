/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import java.util.NavigableMap;
import java.util.TreeMap;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SortedMapFactory implements PluralObjectFactory<NavigableMap<?, ?>> {

    public static final SortedMapFactory INSTANCE = new SortedMapFactory();

    @Override
    public NavigableMap<?, ?> createCollection(int size) {
        return new TreeMap<>();
    }

}
