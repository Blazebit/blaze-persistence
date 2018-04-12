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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapRetainAllValuesAction<C extends Map<K, V>, K, V> implements MapAction<C> {

    private final Collection<?> elements;
    private final Map<K, V> removedObjectsInView;

    public MapRetainAllValuesAction(Collection<?> elements, Map<K, V> delegate) {
        this.elements = elements;
        this.removedObjectsInView = new LinkedHashMap<>(elements.size());
        for (Map.Entry<K, V> entry : delegate.entrySet()) {
            if (!elements.contains(entry.getValue())) {
                this.removedObjectsInView.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private MapRetainAllValuesAction(List<Map.Entry<K, V>> elements, Map<K, V> removedObjectsInView) {
        this.elements = elements;
        this.removedObjectsInView = removedObjectsInView;
    }

    @Override
    public void doAction(C map, UpdateContext context, MapViewToEntityMapper mapper, CollectionRemoveListener keyRemoveListener, CollectionRemoveListener valueRemoveListener) {
        if (mapper != null && mapper.getValueMapper() != null) {
            List<Object> mappedElements = new ArrayList<>(elements.size());
            for (Object e : elements) {
                mappedElements.add(mapper.getValueMapper().applyToEntity(context, null, e));
            }
            retainValues(context, map, mappedElements, keyRemoveListener, valueRemoveListener);
        } else {
            retainValues(context, map, elements, keyRemoveListener, valueRemoveListener);
        }
    }

    private void retainValues(UpdateContext context, C map, Collection<?> elements, CollectionRemoveListener keyRemoveListener, CollectionRemoveListener valueRemoveListener) {
        if (keyRemoveListener != null || valueRemoveListener != null) {
            Iterator<Map.Entry<K, V>> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<K, V> entry = iter.next();
                if (!elements.contains(entry.getValue())) {
                    if (keyRemoveListener != null) {
                        keyRemoveListener.onCollectionRemove(context, entry.getKey());
                    }
                    if (valueRemoveListener != null) {
                        valueRemoveListener.onCollectionRemove(context, removedObjectsInView.get(entry.getKey()));
                    }
                    iter.remove();
                }
            }
        } else {
            map.values().retainAll(elements);
        }
    }

    @Override
    public Collection<Object> getAddedKeys() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedKeys() {
        return (Collection<Object>) removedObjectsInView.keySet();
    }

    @Override
    public Collection<Object> getAddedElements() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedElements() {
        return (Collection<Object>) removedObjectsInView.values();
    }

    @Override
    public Collection<Object> getAddedKeys(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedKeys(C collection) {
        List<Object> removedObjects = new ArrayList<>(collection.size());
        for (Map.Entry<K, V> entry : collection.entrySet()) {
            if (!elements.contains(entry.getValue())) {
                K k = entry.getKey();
                if (k != null) {
                    removedObjects.add(k);
                }
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
            if (!elements.contains(entry.getValue())) {
                V v = entry.getValue();
                if (v != null) {
                    removedObjects.add(v);
                }
            }
        }
        return removedObjects;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapAction<C> replaceObject(Object oldKey, Object oldValue, Object newKey, Object newValue) {
        List<Object> newElements = RecordingUtils.replaceElements(elements, oldValue, newValue);

        if (newElements == null) {
            return null;
        }
        return new MapRetainAllValuesAction(newElements, removedObjectsInView);
    }

    @Override
    public void addAction(List<MapAction<C>> actions, Collection<Object> addedKeys, Collection<Object> removedKeys, Collection<Object> addedElements, Collection<Object> removedElements) {
        actions.add(this);
    }

}
