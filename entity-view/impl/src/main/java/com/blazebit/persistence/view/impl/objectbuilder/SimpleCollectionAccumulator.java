/*
 * Copyright 2014 - 2020 Blazebit.
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
public class SimpleCollectionAccumulator implements ContainerAccumulator<Collection<Object>>  {

    private final PluralObjectFactory<Collection<Object>> pluralObjectFactory;
    private final boolean forceUnique;
    private final Comparator<Object> comparator;

    public SimpleCollectionAccumulator(PluralObjectFactory<? extends Collection<?>> pluralObjectFactory, boolean forceUnique, Comparator<Object> comparator) {
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
        container.add(value);
    }

    @Override
    public void addAll(Collection<Object> container, Collection<Object> collection, boolean recording) {
        container.addAll(collection);
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
