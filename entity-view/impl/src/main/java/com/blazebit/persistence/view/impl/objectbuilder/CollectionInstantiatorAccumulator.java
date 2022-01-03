/*
 * Copyright 2014 - 2022 Blazebit.
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

import com.blazebit.persistence.view.impl.collection.CollectionInstantiatorImplementor;
import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.collection.RecordingList;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class CollectionInstantiatorAccumulator implements ContainerAccumulator<Collection<?>>  {

    private final CollectionInstantiatorImplementor<?, ?> collectionInstantiator;
    private final ContainerAccumulator<Object> valueAccumulator;
    private final boolean filterNulls;

    public CollectionInstantiatorAccumulator(CollectionInstantiatorImplementor<?, ?> collectionInstantiator, ContainerAccumulator<?> valueAccumulator, boolean filterNulls) {
        this.collectionInstantiator = collectionInstantiator;
        this.valueAccumulator = (ContainerAccumulator<Object>) valueAccumulator;
        this.filterNulls = filterNulls;
    }

    public CollectionInstantiatorImplementor<?, ?> getCollectionInstantiator() {
        return collectionInstantiator;
    }

    public ContainerAccumulator<Object> getValueAccumulator() {
        return valueAccumulator;
    }

    @Override
    public Collection<?> createContainer(boolean recording, int size) {
        if (recording) {
            return collectionInstantiator.createRecordingCollection(size);
        } else {
            return collectionInstantiator.createCollection(size);
        }
    }

    @Override
    public void add(Collection<?> container, Object indexObject, Object value, boolean recording) {
        if (filterNulls && value == null) {
            return;
        }
        if (indexObject == null || !collectionInstantiator.isIndexed()) {
            if (recording) {
                ((RecordingCollection<?, Object>) container).getDelegate().add(value);
            } else {
                ((Collection<Object>) container).add(value);
            }
        } else {
            final List<Object> list;
            if (recording) {
                list = ((RecordingList<Object>) container).getDelegate();
            } else {
                list = (List<Object>) container;
            }
            int index = (int) indexObject;
            int size = list.size();
            if (index < size) {
                if (valueAccumulator == null) {
                    Object oldValue = list.set(index, value);
                    if (oldValue != null && !oldValue.equals(value)) {
                        throw new IllegalArgumentException("Value " + value + " replaces old value " + oldValue + " at index " + index + "! Use a proper accumulator!");
                    }
                } else {
                    valueAccumulator.add(list.get(index), null, value, false);
                }
            } else {
                if (index > size) {
                    for (int i = size; i < index; i++) {
                        list.add(null);
                    }
                }
                if (valueAccumulator != null) {
                    Object valueContainer = valueAccumulator.createContainer(false, 1);
                    valueAccumulator.add(valueContainer, null, value, false);
                    value = valueContainer;
                }
                list.add(index, value);
            }
        }
    }

    @Override
    public void addAll(Collection<?> container, Collection<?> collection, boolean recording) {
        if (!collectionInstantiator.isIndexed() || !(collection instanceof List<?>)) {
            Collection<Object> target;
            if (recording) {
                target = ((RecordingCollection<?, Object>) container).getDelegate();
            } else {
                target = (Collection<Object>) container;
            }
            if (filterNulls) {
                for (Object o : collection) {
                    if (o != null) {
                        target.add(o);
                    }
                }
            } else {
                target.addAll(collection);
            }
        } else {
            final List<Object> other = (List<Object>) collection;
            final List<Object> list;
            if (recording) {
                list = ((RecordingList<Object>) container).getDelegate();
            } else {
                list = (List<Object>) container;
            }

            int listEnd = Math.min(list.size(), other.size());
            if (valueAccumulator == null) {
                for (int i = 0; i < listEnd; i++) {
                    Object value = other.get(i);
                    Object oldValue = list.set(i, value);
                    if (oldValue != null && !oldValue.equals(value)) {
                        throw new IllegalArgumentException("Value " + value + " replaces old value " + oldValue + " at index " + i + "! Use a proper accumulator!");
                    }
                }
                for (int i = listEnd; i < other.size(); i++) {
                    list.add(i, other.get(i));
                }
            } else {
                for (int i = 0; i < listEnd; i++) {
                    valueAccumulator.add(list.get(i), null, other.get(i), false);
                }
                for (int i = listEnd; i < other.size(); i++) {
                    Object valueContainer = valueAccumulator.createContainer(false, 1);
                    valueAccumulator.addAll(valueContainer, other.get(i), false);
                    list.add(i, valueContainer);
                }
            }
        }
    }

    @Override
    public boolean requiresPostConstruct() {
        return collectionInstantiator.requiresPostConstruct();
    }

    @Override
    public void postConstruct(Collection<?> collection) {
        collectionInstantiator.postConstruct(collection);
    }
}
