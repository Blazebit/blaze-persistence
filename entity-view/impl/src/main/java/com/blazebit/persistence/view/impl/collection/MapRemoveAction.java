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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapRemoveAction<C extends Map<K, V>, K, V> implements MapAction<C> {

    private final Object key;

    public MapRemoveAction(Object key) {
        this.key = key;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doAction(C map, UpdateContext context, MapViewToEntityMapper mapper, CollectionRemoveListener keyRemoveListener, CollectionRemoveListener valueRemoveListener) {
        V value;
        if (mapper != null && mapper.getKeyMapper() != null) {
            value = map.remove(mapper.getKeyMapper().applyToEntity(context, null, key));
        } else {
            value = map.remove(key);
        }

        if (value != null) {
            if (keyRemoveListener != null) {
                keyRemoveListener.onCollectionRemove(context, key);
            }
            if (valueRemoveListener != null) {
                valueRemoveListener.onCollectionRemove(context, value);
            }
        }
    }

    @Override
    public Collection<Object> getAddedObjects(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedObjects(C collection) {
        V value = collection.get(key);
        if (value == null) {
            return Collections.singletonList(key);
        }
        return Arrays.asList(key, value);
    }

    @Override
    public Collection<Object> getAddedKeys(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedKeys(C collection) {
        return Collections.singleton(key);
    }

    @Override
    public Collection<Object> getAddedElements(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedElements(C collection) {
        V value = collection.get(key);
        if (value == null) {
            return Collections.emptyList();
        }

        return (Collection<Object>) Collections.singleton(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapAction<C> replaceObject(Object oldKey, Object oldValue, Object newKey, Object newValue) {
        if (oldKey == key) {
            return new MapRemoveAction(newKey);
        }
        return null;
    }

    @Override
    public void addAction(List<MapAction<C>> actions, Collection<Object> addedKeys, Collection<Object> removedKeys, Collection<Object> addedElements, Collection<Object> removedElements) {
        actions.add(this);
    }

}
