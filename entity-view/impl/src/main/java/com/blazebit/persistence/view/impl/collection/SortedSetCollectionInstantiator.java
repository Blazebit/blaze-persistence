/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import java.util.Collection;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SortedSetCollectionInstantiator  extends AbstractCollectionInstantiator<NavigableSet<?>, RecordingNavigableSet<?>> {

    private final Set<Class<?>> allowedSubtypes;
    private final Set<Class<?>> parentRequiringUpdateSubtypes;
    private final Set<Class<?>> parentRequiringCreateSubtypes;
    private final boolean updatable;
    private final boolean optimize;
    private final boolean strictCascadingCheck;
    private final Comparator<?> comparator;

    public SortedSetCollectionInstantiator(PluralObjectFactory<Collection<?>> collectionFactory, Set<Class<?>> allowedSubtypes, Set<Class<?>> parentRequiringUpdateSubtypes, Set<Class<?>> parentRequiringCreateSubtypes, boolean updatable, boolean optimize, boolean strictCascadingCheck, Comparator<?> comparator) {
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
    public boolean allowsDuplicates() {
        return false;
    }

    @Override
    public boolean isIndexed() {
        return false;
    }

    @Override
    public NavigableSet<?> createCollection(int size) {
        return new TreeSet(comparator);
    }

    @Override
    public RecordingNavigableSet<?> createRecordingCollection(int size) {
        return new RecordingNavigableSet(createCollection(size), allowedSubtypes, parentRequiringUpdateSubtypes, parentRequiringCreateSubtypes, updatable, optimize, strictCascadingCheck);
    }
}
