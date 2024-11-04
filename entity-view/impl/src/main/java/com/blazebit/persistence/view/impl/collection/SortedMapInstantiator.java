/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SortedMapInstantiator extends AbstractMapInstantiator<NavigableMap<?, ?>, RecordingNavigableMap<NavigableMap<?, ?>, ?, ?>> {

    private final Set<Class<?>> allowedSubtypes;
    private final Set<Class<?>> parentRequiringUpdateSubtypes;
    private final Set<Class<?>> parentRequiringCreateSubtypes;
    private final boolean updatable;
    private final boolean optimize;
    private final boolean strictCascadingCheck;
    private final Comparator<?> comparator;

    public SortedMapInstantiator(PluralObjectFactory<Map<?, ?>> collectionFactory, Set<Class<?>> allowedSubtypes, Set<Class<?>> parentRequiringUpdateSubtypes, Set<Class<?>> parentRequiringCreateSubtypes, boolean updatable, boolean optimize, boolean strictCascadingCheck, Comparator<?> comparator) {
        super(collectionFactory);
        this.allowedSubtypes = allowedSubtypes;
        this.parentRequiringUpdateSubtypes = parentRequiringUpdateSubtypes;
        this.parentRequiringCreateSubtypes = parentRequiringCreateSubtypes;
        this.updatable = updatable;
        this.optimize = optimize;
        this.strictCascadingCheck = strictCascadingCheck;
        this.comparator = comparator;
    }

    @Override
    public NavigableMap<?, ?> createMap(int size) {
        return new TreeMap(comparator);
    }

    @Override
    public RecordingNavigableMap<NavigableMap<?, ?>, ?, ?> createRecordingMap(int size) {
        return new RecordingNavigableMap(createMap(size), allowedSubtypes, parentRequiringUpdateSubtypes, parentRequiringCreateSubtypes, updatable, optimize, strictCascadingCheck);
    }
}
