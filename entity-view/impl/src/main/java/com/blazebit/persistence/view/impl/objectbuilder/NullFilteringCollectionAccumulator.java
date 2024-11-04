/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder;

import com.blazebit.persistence.view.impl.collection.PluralObjectFactory;

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
 * @since 1.5.0
 */
public class NullFilteringCollectionAccumulator implements ContainerAccumulator<Collection<Object>>  {

    private final PluralObjectFactory<Collection<Object>> pluralObjectFactory;
    private final boolean forceUnique;
    private final Comparator<Object> comparator;

    public NullFilteringCollectionAccumulator(PluralObjectFactory<? extends Collection<?>> pluralObjectFactory, boolean forceUnique, Comparator<Object> comparator) {
        this.pluralObjectFactory = (PluralObjectFactory<Collection<Object>>) pluralObjectFactory;
        this.forceUnique = forceUnique;
        this.comparator = comparator;
    }

    @Override
    public Collection<Object> createContainer(boolean recording, int size) {
        return pluralObjectFactory.createCollection(size);
    }

    @Override
    public void add(Collection<Object> container, Object indexObject, Object value, boolean recording) {
        if (value != null) {
            container.add(value);
        }
    }

    @Override
    public void addAll(Collection<Object> container, Collection<Object> collection, boolean recording) {
        for (Object o : collection) {
            if (o != null) {
                container.add(o);
            }
        }
    }

    @Override
    public boolean requiresPostConstruct() {
        return forceUnique || comparator != null;
    }

    @Override
    public void postConstruct(Collection<Object> collection) {
        ArrayList<Object> list = (ArrayList<Object>) collection;
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
}
