/*
 * Copyright 2014 - 2017 Blazebit.
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

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SortedSetCollectionInstantiator implements CollectionInstantiator {

    private final Set<Class<?>> allowedSubtypes;
    private final boolean updatable;
    private final Comparator<?> comparator;

    public SortedSetCollectionInstantiator(Set<Class<?>> allowedSubtypes, boolean updatable, Comparator<?> comparator) {
        this.allowedSubtypes = allowedSubtypes;
        this.updatable = updatable;
        this.comparator = comparator;
    }

    @Override
    public NavigableSet<?> createCollection(int size) {
        return new TreeSet(comparator);
    }

    @Override
    public RecordingSortedSet<NavigableSet<?>, ?> createRecordingCollection(int size) {
        return new RecordingNavigableSet(createCollection(size), allowedSubtypes, updatable);
    }
}
