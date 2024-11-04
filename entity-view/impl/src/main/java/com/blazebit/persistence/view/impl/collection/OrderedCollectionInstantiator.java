/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class OrderedCollectionInstantiator extends AbstractCollectionInstantiator<Collection<?>, RecordingCollection<?, ?>> {

    private final Set<Class<?>> allowedSubtypes;
    private final Set<Class<?>> parentRequiringUpdateSubtypes;
    private final Set<Class<?>> parentRequiringCreateSubtypes;
    private final boolean updatable;
    private final boolean optimize;
    private final boolean forceUnique;
    private final boolean strictCascadingCheck;
    private final Comparator<Object> comparator;

    public OrderedCollectionInstantiator(PluralObjectFactory<Collection<?>> collectionFactory, Set<Class<?>> allowedSubtypes, Set<Class<?>> parentRequiringUpdateSubtypes, Set<Class<?>> parentRequiringCreateSubtypes, boolean updatable, boolean optimize, boolean forceUnique, boolean strictCascadingCheck, Comparator<?> comparator) {
        super(collectionFactory);
        this.allowedSubtypes = allowedSubtypes;
        this.parentRequiringUpdateSubtypes = parentRequiringUpdateSubtypes;
        this.parentRequiringCreateSubtypes = parentRequiringCreateSubtypes;
        this.updatable = updatable;
        this.optimize = optimize;
        this.forceUnique = forceUnique;
        this.strictCascadingCheck = strictCascadingCheck;
        this.comparator = (Comparator<Object>) comparator;
    }

    @Override
    public boolean allowsDuplicates() {
        return true;
    }

    @Override
    public boolean isIndexed() {
        return false;
    }

    @Override
    public boolean requiresPostConstruct() {
        return forceUnique || comparator != null;
    }

    @Override
    public void postConstruct(Collection<?> collection) {
        ArrayList<Object> list;
        if (collection instanceof RecordingCollection<?, ?>) {
            list = (ArrayList<Object>) ((RecordingCollection<?, Object>) collection).getDelegate();
        } else {
            list = (ArrayList<Object>) collection;
        }
        if (forceUnique) {
            Set<Object> set = new HashSet<>(list.size());
            Iterator<Object> iter = list.iterator();

            while (iter.hasNext()) {
                Object o = iter.next();
                if (!set.add(o)) {
                    iter.remove();
                }
            }
        }
        if (comparator != null) {
            Collections.sort(list, comparator);
        }
    }

    @Override
    public Collection<?> createCollection(int size) {
        return new ArrayList<>(size);
    }

    @Override
    public RecordingCollection<Collection<?>, ?> createRecordingCollection(int size) {
        return new RecordingCollection(createCollection(size), false, true, allowedSubtypes, parentRequiringUpdateSubtypes, parentRequiringCreateSubtypes, updatable, optimize, strictCascadingCheck);
    }
}
