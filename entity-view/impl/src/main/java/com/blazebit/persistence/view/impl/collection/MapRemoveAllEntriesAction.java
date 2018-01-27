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
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapRemoveAllEntriesAction<C extends Map<K, V>, K, V> implements MapAction<C> {

    private final Collection<Map.Entry<K, V>> elements;

    public MapRemoveAllEntriesAction(Map.Entry<K, V> entry) {
        this(new ArrayList<>(Collections.singleton(entry)));
    }

    public MapRemoveAllEntriesAction(Collection<Map.Entry<K, V>> elements) {
        this.elements = elements;
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
                        keyRemoveListener.onCollectionRemove(context, k);
                    }
                    if (valueRemoveListener != null && v != null) {
                        valueRemoveListener.onCollectionRemove(context, v);
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
                            valueRemoveListener.onCollectionRemove(context, v);
                        }
                    }
                }
            } else {
                map.entrySet().removeAll(elements);
            }
        }
    }

    @Override
    public Collection<Object> getAddedObjects(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedObjects(C collection) {
        List<Object> list = new ArrayList<>(elements.size() * 2);
        for (Map.Entry<K, V> entry : elements) {
            K k = entry.getKey();
            V v = entry.getValue();
            if (k != null) {
                list.add(k);
            }
            if (v != null) {
                list.add(v);
            }
        }
        return list;
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
        Collection<Map.Entry<Object, Object>> newElements = RecordingUtils.replaceEntries(elements, oldKey, oldValue, newKey, newValue);

        if (newElements == null) {
            return null;
        }
        return new MapRemoveAllEntriesAction(newElements);
    }

    @Override
    public void addAction(List<MapAction<C>> actions, Collection<Object> addedKeys, Collection<Object> removedKeys, Collection<Object> addedElements, Collection<Object> removedElements) {
        actions.add(this);
    }

}
