/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import com.blazebit.persistence.view.impl.entity.MapViewToEntityMapper;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface MapAction<T extends Map<?, ?>> extends Serializable {

    public void doAction(T map, UpdateContext context, MapViewToEntityMapper mapper, CollectionRemoveListener keyRemoveListener, CollectionRemoveListener valueRemoveListener);

    public void undo(T map, Collection<?> removedKeys, Collection<?> addedKeys, Collection<?> removedElements, Collection<?> addedElements);

    public Collection<Object> getAddedKeys();

    public Collection<Object> getRemovedKeys();

    public Collection<Object> getAddedElements();

    public Collection<Object> getRemovedElements();

    public Collection<Object> getAddedKeys(T collection);

    public Collection<Object> getRemovedKeys(T collection);

    public Collection<Object> getAddedElements(T collection);

    public Collection<Object> getRemovedElements(T collection);

    public MapAction<T> replaceObject(Object oldKey, Object oldValue, Object newKey, Object newValue);

    public MapAction<T> replaceObjects(Map<Object, Object> objectMapping);

    public void addAction(List<MapAction<T>> actions, Collection<Object> addedKeys, Collection<Object> removedKeys, Collection<Object> addedElements, Collection<Object> removedElements);
}
