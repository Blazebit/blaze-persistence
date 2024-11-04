/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.update.UpdateContext;

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
public class ListAddAllAction<C extends List<E>, E> implements ListAction<C> {

    private final int index;
    private final boolean append;
    private final List<? extends E> elements;
    
    public ListAddAllAction(int index, boolean append, Collection<? extends E> collection) {
        this.index = index;
        this.append = append;
        this.elements = new ArrayList<E>(collection);
    }

    private ListAddAllAction(List<? extends E> collection, int index, boolean append) {
        this.index = index;
        this.elements = collection;
        this.append = append;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doAction(C list, UpdateContext context, ViewToEntityMapper mapper, CollectionRemoveListener removeListener) {
        if (mapper != null) {
            if (list instanceof ArrayList<?>) {
                ((ArrayList<?>) list).ensureCapacity(list.size() + elements.size());
            }
            if (elements.size() == 1) {
                list.add(index, (E) mapper.applyToEntity(context, null, elements.iterator().next()));
            } else {
                List<E> entities = new ArrayList<>(elements);
                mapper.applyAll(context, (List<Object>) entities);
                list.addAll(index, entities);
            }
        } else {
            list.addAll(index, elements);
        }
    }

    @Override
    public void undo(C collection, Collection<?> removedObjects, Collection<?> addedObjects) {
        for (int i = index + elements.size() - 1; i >= index; i--) {
            collection.remove(i);
        }
    }

    @Override
    public boolean containsObject(C collection, Object o) {
        for (Object element : elements) {
            if (element == o) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<Object> getAddedObjects() {
        return (Collection<Object>) elements;
    }

    @Override
    public Collection<Object> getRemovedObjects() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getAddedObjects(C collection) {
        List<Object> objects = new ArrayList<>(elements.size());
        for (Object element : elements) {
            if (!collection.contains(element)) {
                objects.add(element);
            }
        }
        return objects;
    }

    @Override
    public Collection<Object> getRemovedObjects(C collection) {
        return Collections.emptyList();
    }

    @Override
    public List<Map.Entry<Object, Integer>> getInsertedObjectEntries() {
        if (append) {
            return Collections.emptyList();
        } else {
            List<Map.Entry<Object, Integer>> list = new ArrayList<>(elements.size());
            for (int i = 0; i < elements.size(); i++) {
                list.add(new AbstractMap.SimpleEntry<Object, Integer>(elements.get(i), index + i));
            }
            return list;
        }
    }

    @Override
    public List<Map.Entry<Object, Integer>> getAppendedObjectEntries() {
        if (append) {
            List<Map.Entry<Object, Integer>> list = new ArrayList<>(elements.size());
            for (int i = 0; i < elements.size(); i++) {
                list.add(new AbstractMap.SimpleEntry<Object, Integer>(elements.get(i), index + i));
            }
            return list;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Map.Entry<Object, Integer>> getRemovedObjectEntries() {
        return Collections.emptyList();
    }

    @Override
    public List<Map.Entry<Object, Integer>> getTrimmedObjectEntries() {
        return Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public CollectionAction<C> replaceObject(Object oldElem, Object elem) {
        List<Object> newElements = RecordingUtils.replaceElements(elements, oldElem, elem);

        if (newElements == null) {
            return null;
        }
        return new ListAddAllAction(index, append, newElements);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CollectionAction<C> replaceObjects(Map<Object, Object> objectMapping) {
        List<Object> newElements = RecordingUtils.replaceElements(elements, objectMapping);

        if (newElements == null) {
            return new ListAddAllAction<>(index, append, elements);
        }
        return new ListAddAllAction(newElements, index, append);
    }

    @Override
    public void addAction(RecordingCollection<?, ?> recordingCollection, List<CollectionAction<C>> actions) {
        actions.add(this);
    }

}
