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

import com.blazebit.persistence.view.impl.entity.MapViewToEntityMapper;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;

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
public class MapPutAllAction<C extends Map<K, V>, K, V> implements MapAction<C> {

    private final Map<? extends K, ? extends V> elements;
    private final Map<K, V> removedObjectsInView;
    
    public MapPutAllAction(Map<? extends K, ? extends V> map, Map<K, V> delegate) {
        this.elements = new LinkedHashMap<>(map);
        this.removedObjectsInView = new LinkedHashMap<>(elements.size());
        for (Map.Entry<? extends K, ? extends V> entry : elements.entrySet()) {
            V oldValue = delegate.get(entry.getKey());
            if (oldValue != null) {
                this.removedObjectsInView.put(entry.getKey(), oldValue);
            }
        }
    }

    private MapPutAllAction(Map<? extends K, ? extends V> map, Map<K, V> removedObjectsInView, boolean a) {
        this.elements = map;
        this.removedObjectsInView = removedObjectsInView;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doAction(C map, UpdateContext context, MapViewToEntityMapper mapper, CollectionRemoveListener keyRemoveListener, CollectionRemoveListener valueRemoveListener) {
        if (mapper != null) {
            ViewToEntityMapper keyMapper = mapper.getKeyMapper();
            ViewToEntityMapper valueMapper = mapper.getValueMapper();

            if (elements.size() == 1) {
                Map.Entry<? extends K, ? extends V> e = elements.entrySet().iterator().next();
                K k = e.getKey();
                V v = e.getValue();

                if (keyMapper != null) {
                    k = (K) keyMapper.applyToEntity(context, null, k);
                }
                if (valueMapper != null) {
                    v = (V) valueMapper.applyToEntity(context, null, v);
                }

                V oldValue = map.put(k, v);
                if (valueRemoveListener != null && oldValue != null) {
                    valueRemoveListener.onCollectionRemove(context, removedObjectsInView.get(e.getKey()));
                }
            } else {
                if (keyMapper == null) {
                    if (valueMapper == null) {
                        for (Map.Entry<? extends K, ? extends V> e : elements.entrySet()) {
                            V oldValue = map.put(e.getKey(), e.getValue());
                            if (valueRemoveListener != null && oldValue != null) {
                                valueRemoveListener.onCollectionRemove(context, removedObjectsInView.get(e.getKey()));
                            }
                        }
                    } else {
                        List<V> entities = new ArrayList<>(elements.size());
                        entities.addAll(elements.values());
                        valueMapper.applyAll(context, (List<Object>) entities);
                        int i = 0;
                        for (Map.Entry<? extends K, ? extends V> e : elements.entrySet()) {
                            V value = entities.get(i++);
                            V oldValue = map.put(e.getKey(), value);
                            if (valueRemoveListener != null && oldValue != null) {
                                valueRemoveListener.onCollectionRemove(context, removedObjectsInView.get(e.getKey()));
                            }
                        }
                    }
                } else if (valueMapper == null) {
                    List<K> entities = new ArrayList<>(elements.size());
                    entities.addAll(elements.keySet());
                    keyMapper.applyAll(context, (List<Object>) entities);
                    int i = 0;
                    for (Map.Entry<? extends K, ? extends V> e : elements.entrySet()) {
                        K key = entities.get(i++);
                        V oldValue = map.put(key, e.getValue());
                        if (valueRemoveListener != null && oldValue != null) {
                            valueRemoveListener.onCollectionRemove(context, removedObjectsInView.get(e.getKey()));
                        }
                    }
                } else {
                    List<K> keyEntities = new ArrayList<>(elements.size());
                    List<V> valueEntities = new ArrayList<>(elements.size());
                    for (Map.Entry<? extends K, ? extends V> entry : elements.entrySet()) {
                        keyEntities.add(entry.getKey());
                        valueEntities.add(entry.getValue());
                    }
                    keyMapper.applyAll(context, (List<Object>) keyEntities);
                    valueMapper.applyAll(context, (List<Object>) valueEntities);
                    int i = 0;
                    for (Map.Entry<? extends K, ? extends V> e : elements.entrySet()) {
                        K key = keyEntities.get(i);
                        V value = valueEntities.get(i++);
                        V oldValue = map.put(key, value);
                        if (valueRemoveListener != null && oldValue != null) {
                            valueRemoveListener.onCollectionRemove(context, removedObjectsInView.get(e.getKey()));
                        }
                    }
                }
            }
        } else {
            if (map.size() > 0 && valueRemoveListener != null) {
                for (Map.Entry<? extends K, ? extends V> e : elements.entrySet()) {
                    V oldValue = map.put(e.getKey(), e.getValue());
                    if (oldValue != null) {
                        valueRemoveListener.onCollectionRemove(context, removedObjectsInView.get(e.getKey()));
                    }
                }
            } else {
                map.putAll(elements);
            }
        }
    }

    @Override
    public void undo(C map, Collection<?> removedKeys, Collection<?> addedKeys, Collection<?> removedElements, Collection<?> addedElements) {
        for (Map.Entry<K, V> entry : removedObjectsInView.entrySet()) {
            if (addedKeys.contains(entry.getKey()) || addedElements.contains(entry.getValue())) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public Collection<Object> getAddedKeys() {
        return (Collection<Object>) elements.keySet();
    }

    @Override
    public Collection<Object> getRemovedKeys() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getAddedElements() {
        return (Collection<Object>) elements.values();
    }

    @Override
    public Collection<Object> getRemovedElements() {
        return (Collection<Object>) removedObjectsInView.values();
    }

    @Override
    public Collection<Object> getAddedKeys(C collection) {
        return (Collection<Object>) elements.keySet();
    }

    @Override
    public Collection<Object> getRemovedKeys(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getAddedElements(C collection) {
        return (Collection<Object>) elements.values();
    }

    @Override
    public Collection<Object> getRemovedElements(C collection) {
        List<Object> removedObjects = new ArrayList<>();
        for (Map.Entry<? extends K, ? extends V> entry : elements.entrySet()) {
            V oldValue = collection.get(entry.getKey());
            if (oldValue != null && !oldValue.equals(entry.getValue())) {
                removedObjects.add(oldValue);
            }
        }
        return removedObjects;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapAction<C> replaceObject(Object oldKey, Object oldValue, Object newKey, Object newValue) {
        Map<Object, Object> newElements = RecordingUtils.replaceElements(elements, oldKey, oldValue, newKey, newValue);

        if (newElements == null) {
            return null;
        }
        return new MapPutAllAction(newElements, removedObjectsInView, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapAction<C> replaceObjects(Map<Object, Object> objectMapping) {
        if (objectMapping == null) {
            return this;
        }
        Map<Object, Object> newElements = RecordingUtils.replaceElements(elements, objectMapping);
        Map<Object, Object> newRemovedObjectsInView = RecordingUtils.replaceElements(removedObjectsInView, objectMapping);

        if (newElements != null) {
            if (newRemovedObjectsInView == null) {
                return new MapPutAllAction(newElements, removedObjectsInView, false);
            } else {
                return new MapPutAllAction(newElements, newRemovedObjectsInView, false);
            }
        } else if (newRemovedObjectsInView != null) {
            return new MapPutAllAction(elements, newRemovedObjectsInView, false);
        } else {
            return this;
        }
    }

    @Override
    public void addAction(List<MapAction<C>> actions, Collection<Object> addedKeys, Collection<Object> removedKeys, Collection<Object> addedElements, Collection<Object> removedElements) {
        actions.add(this);
    }

}
