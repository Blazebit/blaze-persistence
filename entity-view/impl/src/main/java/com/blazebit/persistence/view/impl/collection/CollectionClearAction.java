/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CollectionClearAction<C extends Collection<E>, E> implements CollectionAction<C> {

    @Override
    public void doAction(C collection, UpdateContext context, ViewToEntityMapper mapper, CollectionRemoveListener removeListener) {
        if (removeListener != null) {
            for (E e : collection) {
                removeListener.onCollectionRemove(context, e);
            }
        }
        collection.clear();
    }

    @Override
    public void undo(C collection, Collection<?> removedObjects, Collection<?> addedObjects) {
        throw new UnsupportedOperationException("Can't undo clear!");
    }

    @Override
    public boolean containsObject(C collection, Object o) {
        // Trivially, a clear action contains all objects
        return true;
    }

    @Override
    public Collection<Object> getAddedObjects() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedObjects() {
        // The clear action is used internally only, so this shouldn't matter
        return null;
    }

    @Override
    public Collection<Object> getAddedObjects(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedObjects(C collection) {
        return (Collection<Object>) collection;
    }

    @Override
    public CollectionAction<C> replaceObject(Object oldElem, Object elem) {
        return null;
    }

    @Override
    public CollectionAction<C> replaceObjects(Map<Object, Object> objectMapping) {
        return this;
    }

    @Override
    public void addAction(RecordingCollection<?, ?> recordingCollection, List<CollectionAction<C>> actions) {
        actions.clear();
        actions.add(this);
    }
}
