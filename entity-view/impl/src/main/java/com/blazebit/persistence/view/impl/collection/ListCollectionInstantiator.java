/*
 * Copyright 2014 - 2021 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.view.impl.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ListCollectionInstantiator extends AbstractCollectionInstantiator<List<?>, RecordingList<?>> {

    private final Set<Class<?>> allowedSubtypes;
    private final Set<Class<?>> parentRequiringUpdateSubtypes;
    private final Set<Class<?>> parentRequiringCreateSubtypes;
    private final boolean updatable;
    private final boolean indexed;
    private final boolean optimize;
    private final boolean forceUnique;
    private final boolean strictCascadingCheck;
    private final Comparator<Object> comparator;

    public ListCollectionInstantiator(PluralObjectFactory<Collection<?>> collectionFactory, Set<Class<?>> allowedSubtypes, Set<Class<?>> parentRequiringUpdateSubtypes, Set<Class<?>> parentRequiringCreateSubtypes, boolean updatable, boolean indexed, boolean optimize, boolean forceUnique, boolean strictCascadingCheck, Comparator<?> comparator) {
        super(collectionFactory);
        this.allowedSubtypes = allowedSubtypes;
        this.parentRequiringUpdateSubtypes = parentRequiringUpdateSubtypes;
        this.parentRequiringCreateSubtypes = parentRequiringCreateSubtypes;
        this.updatable = updatable;
        this.indexed = indexed;
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
        return indexed;
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
    public List<?> createCollection(int size) {
        return new ArrayList<>(size);
    }

    @Override
    public RecordingList<?> createRecordingCollection(int size) {
        return new RecordingList(createCollection(size), indexed, allowedSubtypes, parentRequiringUpdateSubtypes, parentRequiringCreateSubtypes, updatable, optimize, strictCascadingCheck);
    }
}
