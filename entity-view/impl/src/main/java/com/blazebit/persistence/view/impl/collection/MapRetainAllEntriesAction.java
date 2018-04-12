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
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapRetainAllEntriesAction<C extends Map<K, V>, K, V> implements MapAction<C> {

    private final Collection<Map.Entry<K, V>> elements;
    private final Map<K, V> removedObjectsInView;

    public MapRetainAllEntriesAction(Collection<Map.Entry<K, V>> elements, Map<K, V> delegate) {
        this.elements = elements;
        this.removedObjectsInView = new LinkedHashMap<>(elements.size());
        for (Map.Entry<K, V> entry : delegate.entrySet()) {
            if (!elements.contains(entry)) {
                this.removedObjectsInView.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private MapRetainAllEntriesAction(List<Map.Entry<K, V>> elements, Map<K, V> removedObjectsInView) {
        this.elements = elements;
        this.removedObjectsInView = removedObjectsInView;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doAction(C map, UpdateContext context, MapViewToEntityMapper mapper, CollectionRemoveListener keyRemoveListener, CollectionRemoveListener valueRemoveListener) {
        if (mapper != null) {
            List<Map.Entry<K, V>> mappedElements = new ArrayList<>(elements.size());
            ViewToEntityMapper keyMapper = mapper.getKeyMapper();
            ViewToEntityMapper valueMapper = mapper.getValueMapper();

            for (Map.Entry<K, V> entry : elements) {
                K k = entry.getKey();
                V v = entry.getValue();

                if (keyMapper != null) {
                    k = (K) keyMapper.applyToEntity(context, null, k);
                }
                if (valueMapper != null) {
                    v = (V) valueMapper.applyToEntity(context, null, v);
                }

                mappedElements.add(new AbstractMap.SimpleEntry<K, V>(k, v));
            }

            invokeRemoveListeners(context, map, mappedElements, keyRemoveListener, valueRemoveListener);
            map.entrySet().retainAll(mappedElements);
        } else {
            invokeRemoveListeners(context, map, elements, keyRemoveListener, valueRemoveListener);
            map.entrySet().retainAll(elements);
        }
    }

    private void invokeRemoveListeners(UpdateContext context, C map, Collection<Map.Entry<K, V>> elements, CollectionRemoveListener keyRemoveListener, CollectionRemoveListener valueRemoveListener) {
        if (keyRemoveListener != null || valueRemoveListener != null) {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                if (!elements.contains(entry)) {
                    if (keyRemoveListener != null) {
                        keyRemoveListener.onCollectionRemove(context, entry.getKey());
                    }
                    if (valueRemoveListener != null) {
                        valueRemoveListener.onCollectionRemove(context, removedObjectsInView.get(entry.getKey()));
                    }
                }
            }
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
            if (!elements.contains(entry)) {
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
            if (!elements.contains(entry)) {
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
        List<Map.Entry<Object, Object>> newElements = RecordingUtils.replaceEntries(elements, oldKey, oldValue, newKey, newValue);

        if (newElements == null) {
            return null;
        }
        return new MapRetainAllEntriesAction(newElements, removedObjectsInView);
    }

    @Override
    public void addAction(List<MapAction<C>> actions, Collection<Object> addedKeys, Collection<Object> removedKeys, Collection<Object> addedElements, Collection<Object> removedElements) {
        actions.add(this);
    }

}
