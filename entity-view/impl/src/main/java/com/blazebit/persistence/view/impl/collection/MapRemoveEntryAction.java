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

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapRemoveEntryAction<C extends Map<K, V>, K, V> implements MapAction<C> {

    private final Map.Entry<K, V> entry;

    public MapRemoveEntryAction(Map.Entry<K, V> entry) {
        this.entry = entry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doAction(C map, UpdateContext context, MapViewToEntityMapper mapper) {
        if (mapper != null) {
            K k = entry.getKey();
            V v = entry.getValue();

            if (mapper.getKeyMapper() != null) {
                k = (K) mapper.getKeyMapper().applyToEntity(context, null, k);
            }
            if (mapper.getValueMapper() != null) {
                v = (V) mapper.getValueMapper().applyToEntity(context, null, v);
            }

            Map.Entry<K, V> e = new AbstractMap.SimpleEntry<>(k, v);
            map.entrySet().remove(e);
        } else {
            map.entrySet().remove(entry);
        }
    }

    @Override
    public Collection<Object> getAddedObjects(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedObjects(C collection) {
        V value = collection.get(entry.getKey());
        if (value == null) {
            return Collections.emptyList();
        } else if (value.equals(entry.getValue())) {
            if (entry.getKey() == null) {
                return (Collection<Object>) Collections.singleton(entry.getValue());
            } else {
                return Arrays.asList(entry.getKey(), entry.getValue());
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Collection<Object> getAddedKeys(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedKeys(C collection) {
        if (entry.getKey() == null) {
            return Collections.emptyList();
        }
        V value = collection.get(entry.getKey());
        if (value == null || !value.equals(entry.getValue())) {
            return Collections.emptyList();
        }
        return (Collection<Object>) Collections.singletonList(entry.getKey());
    }

    @Override
    public Collection<Object> getAddedElements(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedElements(C collection) {
        V value = collection.get(entry.getKey());
        if (value == null) {
            return Collections.emptyList();
        } else if (value.equals(entry.getValue())) {
            return (Collection<Object>) Collections.singleton(value);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapAction<C> replaceObject(Object oldKey, Object oldValue, Object newKey, Object newValue) {
        if (oldKey == entry.getKey() || oldValue == entry.getValue()) {
            return new MapRemoveEntryAction(new AbstractMap.SimpleEntry<>(newKey, newValue));
        }
        return null;
    }

}
