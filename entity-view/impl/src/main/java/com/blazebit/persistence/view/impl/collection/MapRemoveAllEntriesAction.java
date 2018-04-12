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
public class MapRemoveAllEntriesAction<C extends Map<K, V>, K, V> implements MapAction<C> {

    private final Collection<Map.Entry<K, V>> elements;
    private final Map<K, V> removedObjectsInView;

    public MapRemoveAllEntriesAction(Map.Entry<K, V> entry, Map<K, V> delegate) {
        this((Collection<Map.Entry<K,V>>) new ArrayList<>(Collections.singleton(entry)), delegate);
    }

    public MapRemoveAllEntriesAction(Collection<Map.Entry<K, V>> elements, Map<K, V> delegate) {
        this.elements = elements;
        this.removedObjectsInView = new LinkedHashMap<>(elements.size());
        for (Map.Entry<K, V> entry : elements) {
            V oldValue = delegate.get(entry.getKey());
            if (oldValue != null) {
                this.removedObjectsInView.put(entry.getKey(), oldValue);
            }
        }
    }

    private MapRemoveAllEntriesAction(List<Map.Entry<K, V>> elements, Map<K, V> removedObjectsInView) {
        this.elements = elements;
        this.removedObjectsInView = removedObjectsInView;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doAction(C map, UpdateContext context, MapViewToEntityMapper mapper, CollectionRemoveListener keyRemoveListener, CollectionRemoveListener valueRemoveListener) {
        if (mapper != null) {
            Collection<Map.Entry<K, V>> entrySet = map.entrySet();
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

                Map.Entry<K, V> e = new AbstractMap.SimpleEntry<K, V>(k, v);
                if (entrySet.remove(e)) {
                    if (keyRemoveListener != null && k != null) {
                        keyRemoveListener.onCollectionRemove(context, entry.getKey());
                    }
                    if (valueRemoveListener != null && v != null) {
                        valueRemoveListener.onCollectionRemove(context, removedObjectsInView.get(entry.getKey()));
                    }
                }
            }
        } else {
            if (map.size() > 0 && (keyRemoveListener != null || valueRemoveListener != null)) {
                Collection<Map.Entry<K, V>> entrySet = map.entrySet();
                for (Map.Entry<? extends K, ? extends V> e : elements) {
                    if (entrySet.remove(e)) {
                        K k = e.getKey();
                        V v = e.getValue();
                        if (keyRemoveListener != null && k != null) {
                            keyRemoveListener.onCollectionRemove(context, k);
                        }
                        if (valueRemoveListener != null && v != null) {
                            valueRemoveListener.onCollectionRemove(context, removedObjectsInView.get(k));
                        }
                    }
                }
            } else {
                map.entrySet().removeAll(elements);
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
        List<Object> list = new ArrayList<>(elements.size());
        for (Map.Entry<K, V> entry : elements) {
            K k = entry.getKey();
            if (k != null) {
                list.add(k);
            }
        }
        return list;
    }

    @Override
    public Collection<Object> getAddedElements(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedElements(C collection) {
        List<Object> list = new ArrayList<>(elements.size());
        for (Map.Entry<K, V> entry : elements) {
            V v = entry.getValue();
            if (v != null) {
                list.add(v);
            }
        }
        return list;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapAction<C> replaceObject(Object oldKey, Object oldValue, Object newKey, Object newValue) {
        List<Map.Entry<Object, Object>> newElements = RecordingUtils.replaceEntries(elements, oldKey, oldValue, newKey, newValue);

        if (newElements == null) {
            return null;
        }
        return new MapRemoveAllEntriesAction(newElements, removedObjectsInView);
    }

    @Override
    public void addAction(List<MapAction<C>> actions, Collection<Object> addedKeys, Collection<Object> removedKeys, Collection<Object> addedElements, Collection<Object> removedElements) {
        actions.add(this);
    }

}
