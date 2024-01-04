/*
 * Copyright 2014 - 2024 Blazebit.
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
public class ListRemoveAction<C extends List<E>, E> implements ListAction<C> {

    private final int index;
    private final boolean last;
    private final Object removedElementInView;
    
    public ListRemoveAction(int index, boolean last, List<?> delegate) {
        this.index = index;
        this.last = last;
        this.removedElementInView = delegate.get(index);
    }

    public ListRemoveAction(int index, boolean last, Object removedElementInView) {
        this.index = index;
        this.last = last;
        this.removedElementInView = removedElementInView;
    }

    @Override
    public void doAction(C list, UpdateContext context, ViewToEntityMapper mapper, CollectionRemoveListener removeListener) {
        E removeElement = list.remove(index);
        if (removeListener != null && removeElement != null) {
            removeListener.onCollectionRemove(context, removedElementInView);
        }
    }

    @Override
    public void undo(C collection, Collection<?> removedObjects, Collection<?> addedObjects) {
        collection.add(index, (E) removedElementInView);
    }

    @Override
    public boolean containsObject(C collection, Object o) {
        // For completeness we implemented this, but actually this is never needed
        return collection.size() > index && collection.get(index) == o;
    }

    @Override
    public Collection<Object> getAddedObjects() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedObjects() {
        return Collections.singleton(removedElementInView);
    }

    @Override
    public Collection<Object> getAddedObjects(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedObjects(C collection) {
        return (Collection<Object>) Collections.singleton(collection.get(index));
    }

    @Override
    public List<Map.Entry<Object, Integer>> getInsertedObjectEntries() {
        return Collections.emptyList();
    }

    @Override
    public List<Map.Entry<Object, Integer>> getAppendedObjectEntries() {
        return Collections.emptyList();
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
    public CollectionAction<C> replaceObject(Object oldElem, Object elem) {
        if (oldElem != removedElementInView) {
            return this;
        }

        return new ListRemoveAction(index, last, elem);
    }

    @Override
    public CollectionAction<C> replaceObjects(Map<Object, Object> objectMapping) {
        if (objectMapping == null) {
            return this;
        }
        Object newElement = objectMapping.get(removedElementInView);
        if (newElement == null) {
            return this;
        }

        return new ListRemoveAction(index, last, newElement);
    }

    @Override
    public void addAction(RecordingCollection<?, ?> recordingCollection, List<CollectionAction<C>> actions) {
        actions.add(this);
    }
}
