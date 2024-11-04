/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
public class MapClearAction<C extends Map<K, V>, K, V> implements MapAction<C> {

    @Override
    public void doAction(C map, UpdateContext context, MapViewToEntityMapper mapper, CollectionRemoveListener keyRemoveListener, CollectionRemoveListener valueRemoveListener) {
        if (keyRemoveListener != null || valueRemoveListener != null) {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                if (keyRemoveListener != null) {
                    keyRemoveListener.onCollectionRemove(context, entry.getKey());
                }
                if (valueRemoveListener != null && entry.getValue() != null) {
                    valueRemoveListener.onCollectionRemove(context, entry.getValue());
                }
            }
        }
        map.clear();
    }

    @Override
    public void undo(C map, Collection<?> removedKeys, Collection<?> addedKeys, Collection<?> removedElements, Collection<?> addedElements) {
        throw new UnsupportedOperationException("Can't undo clear!");
    }

    @Override
    public Collection<Object> getAddedKeys() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedKeys() {
        // The clear action is used internally only, so this shouldn't matter
        return null;
    }

    @Override
    public Collection<Object> getAddedElements() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedElements() {
        // The clear action is used internally only, so this shouldn't matter
        return null;
    }

    @Override
    public Collection<Object> getAddedKeys(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedKeys(C collection) {
        return (Collection<Object>) collection.keySet();
    }

    @Override
    public Collection<Object> getAddedElements(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedElements(C collection) {
        return (Collection<Object>) collection.values();
    }

    @Override
    public MapAction<C> replaceObject(Object oldKey, Object oldValue, Object newKey, Object newValue) {
        return null;
    }

    @Override
    public MapAction<C> replaceObjects(Map<Object, Object> objectMapping) {
        return this;
    }

    @Override
    public void addAction(List<MapAction<C>> actions, Collection<Object> addedKeys, Collection<Object> removedKeys, Collection<Object> addedElements, Collection<Object> removedElements) {
        actions.clear();
        actions.add(this);
    }
}
