/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ListSetAction<C extends List<E>, E> implements ListAction<C> {

    final int index;
    final boolean last;
    final E element;
    final E removedElementInView;
    
    public ListSetAction(int index, boolean last, E element, List<?> delegate) {
        this.index = index;
        this.last = last;
        this.element = element;
        this.removedElementInView = delegate == null ? null : (E) delegate.get(index);
    }

    public ListSetAction(int index, boolean last, E element, E removedElementInView) {
        this.index = index;
        this.last = last;
        this.element = element;
        this.removedElementInView = removedElementInView;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doAction(C list, UpdateContext context, ViewToEntityMapper mapper, CollectionRemoveListener removeListener) {
        E removedElement;
        if (mapper == null) {
            removedElement = list.set(index, element);
        } else {
            removedElement = list.set(index, (E) mapper.applyToEntity(context, null, element));
        }

        if (removeListener != null && removedElement != null) {
            removeListener.onCollectionRemove(context, removedElementInView);
        }
    }

    @Override
    public void undo(C collection, Collection<?> removedObjects, Collection<?> addedObjects) {
        collection.set(index, removedElementInView);
    }

    @Override
    public boolean containsObject(C collection, Object o) {
        return element == o;
    }

    @Override
    public Collection<Object> getAddedObjects() {
        return Collections.<Object>singleton(element);
    }

    @Override
    public Collection<Object> getRemovedObjects() {
        return removedElementInView == null ? Collections.emptySet() : Collections.<Object>singleton(removedElementInView);
    }

    @Override
    public Collection<Object> getAddedObjects(C collection) {
        return Collections.<Object>singleton(element);
    }

    @Override
    public Collection<Object> getRemovedObjects(C collection) {
        return Collections.<Object>singleton(collection.get(index));
    }

    @Override
    public List<Map.Entry<Object, Integer>> getInsertedObjectEntries() {
        if (last) {
            return Collections.emptyList();
        } else {
            return Collections.<Map.Entry<Object, Integer>>singletonList(new AbstractMap.SimpleEntry<Object, Integer>(element, index));
        }
    }

    @Override
    public List<Map.Entry<Object, Integer>> getAppendedObjectEntries() {
        if (last) {
            return Collections.<Map.Entry<Object, Integer>>singletonList(new AbstractMap.SimpleEntry<Object, Integer>(element, index));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Map.Entry<Object, Integer>> getRemovedObjectEntries() {
        if (last) {
            return Collections.emptyList();
        } else {
            return Collections.<Map.Entry<Object, Integer>>singletonList(new AbstractMap.SimpleEntry<Object, Integer>(removedElementInView, index));
        }
    }

    @Override
    public List<Map.Entry<Object, Integer>> getTrimmedObjectEntries() {
        if (last) {
            return Collections.<Map.Entry<Object, Integer>>singletonList(new AbstractMap.SimpleEntry<Object, Integer>(removedElementInView, index));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public CollectionAction<C> replaceObject(Object oldElem, Object elem) {
        if (element == oldElem) {
            if (removedElementInView == oldElem) {
                return new ListSetAction(index, last, elem, elem);
            } else {
                return new ListSetAction(index, last, elem, removedElementInView);
            }
        } else if (removedElementInView == oldElem) {
            return new ListSetAction(index, last, element, elem);
        } else {
            return null;
        }
    }

    @Override
    public CollectionAction<C> replaceObjects(Map<Object, Object> objectMapping) {
        if (objectMapping == null) {
            return this;
        }
        Object newElement = objectMapping.get(element);
        Object newRemovedElement = objectMapping.get(removedElementInView);
        if (newElement == null && newRemovedElement == null) {
            return this;
        }
        if (newElement == null) {
            newElement = element;
        }
        if (newRemovedElement == null) {
            newRemovedElement = removedElementInView;
        }

        return new ListSetAction(index, last, newElement, newRemovedElement);
    }

    @Override
    public void addAction(RecordingCollection<?, ?> recordingCollection, List<CollectionAction<C>> actions) {
        CollectionAction<C> lastAction;
        // Multiple set operations are coalesced into a single one
        if (!actions.isEmpty() && (lastAction = actions.get(actions.size() - 1)) instanceof ListSetAction<?, ?>) {
            if (index == ((ListSetAction<?, ?>) lastAction).index) {
                // Don't forget to retain the original removed element
                actions.set(actions.size() - 1, (CollectionAction<C>) new ListSetAction<>(index, last, element, ((ListSetAction<?, ?>) lastAction).removedElementInView));
                return;
            }
        }
        actions.add(this);
    }

}
