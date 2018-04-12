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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapRemoveAllKeysAction<C extends Map<K, V>, K, V> implements MapAction<C> {

    private final Collection<?> elements;
    private final Map<K, V> removedObjectsInView;

    public MapRemoveAllKeysAction(Collection<?> elements, Map<K, V> delegate) {
        this.elements = elements;
        this.removedObjectsInView = new LinkedHashMap<>(elements.size());
        for (Object key : elements) {
            V oldValue = delegate.get(key);
            if (oldValue != null) {
                this.removedObjectsInView.put((K) key, oldValue);
            }
        }
    }

    private MapRemoveAllKeysAction(List<?> elements, Map<K, V> removedObjectsInView) {
        this.elements = elements;
        this.removedObjectsInView = removedObjectsInView;
    }

    @Override
    public void doAction(C map, UpdateContext context, MapViewToEntityMapper mapper, CollectionRemoveListener keyRemoveListener, CollectionRemoveListener valueRemoveListener) {
        if (mapper != null && mapper.getKeyMapper() != null) {
            for (Object e : elements) {
                K key = (K) mapper.getKeyMapper().applyToEntity(context, null, e);
                V value = map.remove(key);
                if (value != null) {
                    if (keyRemoveListener != null) {
                        keyRemoveListener.onCollectionRemove(context, e);
                    }
                    if (valueRemoveListener != null) {
                        valueRemoveListener.onCollectionRemove(context, removedObjectsInView.get(e));
                    }
                }
            }
        } else {
            if (map.size() > 0 && (keyRemoveListener != null || valueRemoveListener != null)) {
                for (Object k : elements) {
                    V v = map.remove(k);
                    if (v != null) {
                        if (keyRemoveListener != null) {
                            keyRemoveListener.onCollectionRemove(context, k);
                        }
                        if (valueRemoveListener != null) {
                            valueRemoveListener.onCollectionRemove(context, removedObjectsInView.get(k));
                        }
                    }
                }
            } else {
                map.keySet().removeAll(elements);
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
        List<Object> removedKeys = new ArrayList<>(elements.size());
        for (Object o : elements) {
            if (collection.containsKey(o)) {
                removedKeys.add(o);
            }
        }
        return removedKeys;
    }

    @Override
    public Collection<Object> getAddedElements(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedElements(C collection) {
        List<Object> removedElements = new ArrayList<>(elements.size());
        for (Object o : elements) {
            V value = collection.get(o);
            if (value != null) {
                removedElements.add(value);
            }
        }
        return removedElements;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapAction<C> replaceObject(Object oldKey, Object oldValue, Object newKey, Object newValue) {
        List<Object> newElements = RecordingUtils.replaceElements(elements, oldKey, newKey);

        if (newElements == null) {
            return null;
        }
        return new MapRemoveAllKeysAction(newElements, removedObjectsInView);
    }

    @Override
    public void addAction(List<MapAction<C>> actions, Collection<Object> addedKeys, Collection<Object> removedKeys, Collection<Object> addedElements, Collection<Object> removedElements) {
        actions.add(this);
    }

}
