/*
 * Copyright 2014 - 2018 Blazebit.
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

import com.blazebit.persistence.view.impl.entity.MapViewToEntityMapper;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapRemoveAllValuesAction<C extends Map<K, V>, K, V> implements MapAction<C> {

    private final Collection<?> elements;

    public MapRemoveAllValuesAction(Object o) {
        this(new ArrayList<>(Collections.singletonList(o)));
    }

    public MapRemoveAllValuesAction(Collection<?> elements) {
        this.elements = elements;
    }

    @Override
    public void doAction(C map, UpdateContext context, MapViewToEntityMapper mapper, CollectionRemoveListener keyRemoveListener, CollectionRemoveListener valueRemoveListener) {
        if (mapper != null && mapper.getValueMapper() != null) {
            for (Object e : elements) {
                V value = (V) mapper.getValueMapper().applyToEntity(context, null, e);
                removeByValue(context, map, value, keyRemoveListener, valueRemoveListener);
            }
        } else {
            if (map.size() > 0 && (keyRemoveListener != null || valueRemoveListener != null)) {
                for (Object e : elements) {
                    removeByValue(context, map, (V) e, keyRemoveListener, valueRemoveListener);
                }
            } else {
                map.values().removeAll(elements);
            }
        }
    }

    private void removeByValue(UpdateContext context, C map, V value, CollectionRemoveListener keyRemoveListener, CollectionRemoveListener valueRemoveListener) {
        Iterator<Map.Entry<K, V>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<K, V> entry = iter.next();
            K k = entry.getKey();
            V v = entry.getValue();

            if (value.equals(v)) {
                if (keyRemoveListener != null) {
                    keyRemoveListener.onCollectionRemove(context, k);
                }
                if (valueRemoveListener != null) {
                    valueRemoveListener.onCollectionRemove(context, v);
                }
            }
        }
    }

    @Override
    public Collection<Object> getAddedObjects(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedObjects(C collection) {
        List<Object> removedObjects = new ArrayList<>(collection.size());
        for (Map.Entry<K, V> entry : collection.entrySet()) {
            if (elements.contains(entry.getValue())) {
                removedObjects.add(entry.getKey());
                removedObjects.add(entry.getValue());
            }
        }
        return removedObjects;
    }

    @Override
    public Collection<Object> getAddedKeys(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedKeys(C collection) {
        List<Object> removedObjects = new ArrayList<>(collection.size());
        for (Map.Entry<K, V> entry : collection.entrySet()) {
            if (elements.contains(entry.getValue())) {
                removedObjects.add(entry.getKey());
            }
        }
        return removedObjects;
    }

    @Override
    public Collection<Object> getAddedElements(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedElements(C collection) {
        List<Object> removedObjects = new ArrayList<>(collection.size());
        for (Map.Entry<K, V> entry : collection.entrySet()) {
            if (elements.contains(entry.getValue())) {
                removedObjects.add(entry.getKey());
            }
        }
        return removedObjects;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapAction<C> replaceObject(Object oldKey, Object oldValue, Object newKey, Object newValue) {
        Collection<Object> newElements = RecordingUtils.replaceElements(elements, oldValue, newValue);

        if (newElements == null) {
            return null;
        }
        return new MapRemoveAllValuesAction(newElements);
    }

    @Override
    public void addAction(List<MapAction<C>> actions, Collection<Object> addedKeys, Collection<Object> removedKeys, Collection<Object> addedElements, Collection<Object> removedElements) {
        actions.add(this);
    }

}
