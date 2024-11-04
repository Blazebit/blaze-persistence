/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface CollectionAction<T extends Collection<?>> extends Serializable {

    public void doAction(T collection, UpdateContext context, ViewToEntityMapper mapper, CollectionRemoveListener removeListener);

    public void undo(T collection, Collection<?> removedObjects, Collection<?> addedObjects);

    public boolean containsObject(T collection, Object o);

    public Collection<Object> getAddedObjects();

    public Collection<Object> getRemovedObjects();

    public Collection<Object> getAddedObjects(T collection);

    public Collection<Object> getRemovedObjects(T collection);

    public CollectionAction<T> replaceObject(Object oldElem, Object elem);

    public CollectionAction<T> replaceObjects(Map<Object, Object> objectMapping);

    public void addAction(RecordingCollection<?, ?> recordingCollection, List<CollectionAction<T>> actions);
}
