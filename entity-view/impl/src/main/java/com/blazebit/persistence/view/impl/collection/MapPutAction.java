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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapPutAction<C extends Map<K, V>, K, V> implements MapAction<C> {

    private final K key;
    private final V value;
    private final V removedValueInView;

    public MapPutAction(K key, V value, Map<K, V> delegate) {
        this.key = key;
        this.value = value;
        this.removedValueInView = delegate.get(key);
    }

    private MapPutAction(K key, V value, V removedValueInView) {
        this.key = key;
        this.value = value;
        this.removedValueInView = removedValueInView;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doAction(C map, UpdateContext context, MapViewToEntityMapper mapper, CollectionRemoveListener keyRemoveListener, CollectionRemoveListener valueRemoveListener) {
        V oldValue;
        if (mapper != null) {
            K k = key;
            V v = value;

            if (mapper.getKeyMapper() != null) {
                k = (K) mapper.getKeyMapper().applyToEntity(context, null, k);
            }
            if (mapper.getValueMapper() != null) {
                v = (V) mapper.getValueMapper().applyToEntity(context, null, v);
            }

            oldValue = map.put(k, v);
        } else {
            oldValue = map.put(key, value);
        }

        if (valueRemoveListener != null && oldValue != null) {
            valueRemoveListener.onCollectionRemove(context, removedValueInView);
        }
    }

    @Override
    public Collection<Object> getAddedKeys() {
        return (Collection<Object>) Collections.singleton(key);
    }

    @Override
    public Collection<Object> getRemovedKeys() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getAddedElements() {
        return (Collection<Object>) Collections.singletonList(value);
    }

    @Override
    public Collection<Object> getRemovedElements() {
        if (removedValueInView != null) {
            return (Collection<Object>) Collections.singleton(removedValueInView);
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getAddedKeys(C collection) {
        return (Collection<Object>) Collections.singleton(key);
    }

    @Override
    public Collection<Object> getRemovedKeys(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getAddedElements(C collection) {
        return (Collection<Object>) Collections.singletonList(value);
    }

    @Override
    public Collection<Object> getRemovedElements(C collection) {
        V oldValue = collection.get(key);
        if (oldValue != null && !oldValue.equals(value)) {
            return (Collection<Object>) Collections.singleton(oldValue);
        }
        return Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapAction<C> replaceObject(Object oldKey, Object oldValue, Object newKey, Object newValue) {
        if (oldKey == key || oldValue == value) {
            return new MapPutAction(newKey, newValue, removedValueInView);
        }
        return null;
    }

    @Override
    public void addAction(List<MapAction<C>> actions, Collection<Object> addedKeys, Collection<Object> removedKeys, Collection<Object> addedElements, Collection<Object> removedElements) {
        actions.add(this);
    }
}
