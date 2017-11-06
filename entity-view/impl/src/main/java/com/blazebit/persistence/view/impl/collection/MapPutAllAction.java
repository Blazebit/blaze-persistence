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
    
    public MapPutAllAction(Map<? extends K, ? extends V> map) {
        this.elements = new LinkedHashMap<K, V>(map);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doAction(C map, UpdateContext context, MapViewToEntityMapper mapper) {
        if (mapper != null) {
            ViewToEntityMapper keyMapper = mapper.getKeyMapper();
            ViewToEntityMapper valueMapper = mapper.getValueMapper();

            for (Map.Entry<? extends K, ? extends V> e : elements.entrySet()) {
                K k = e.getKey();
                V v = e.getValue();

                if (keyMapper != null) {
                    k = (K) keyMapper.applyToEntity(context, null, k);
                }
                if (valueMapper != null) {
                    v = (V) valueMapper.applyToEntity(context, null, v);
                }

                map.put(k, v);
            }
        } else {
            map.putAll(elements);
        }
    }

    @Override
    public Collection<Object> getAddedObjects(C collection) {
        return new MultiCollection(elements.keySet(), elements.values());
    }

    @Override
    public Collection<Object> getRemovedObjects(C collection) {
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
        return getRemovedObjects(collection);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapAction<C> replaceObject(Object oldKey, Object oldValue, Object newKey, Object newValue) {
        Map<Object, Object> newElements = ActionUtils.replaceElements(elements, oldKey, oldValue, newKey, newValue);

        if (newElements == null) {
            return null;
        }
        return new MapPutAllAction(newElements);
    }

}
