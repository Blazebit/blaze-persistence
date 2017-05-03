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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class OrderedSetCollectionInstantiator implements CollectionInstantiator {

    private final Set<Class<?>> allowedSubtypes;
    private final boolean updatable;

    public OrderedSetCollectionInstantiator(Set<Class<?>> allowedSubtypes, boolean updatable) {
        this.allowedSubtypes = allowedSubtypes;
        this.updatable = updatable;
    }

    @Override
    public Set<?> createCollection(int size) {
        return new LinkedHashSet<>(size);
    }

    @Override
    public RecordingSet<Set<?>, ?> createRecordingCollection(int size) {
        return new RecordingSet(createCollection(size), allowedSubtypes, updatable);
    }
}
