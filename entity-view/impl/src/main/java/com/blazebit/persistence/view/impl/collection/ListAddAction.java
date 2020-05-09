/*
 * Copyright 2014 - 2020 Blazebit.
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
public class ListAddAction<C extends List<E>, E> implements ListAction<C> {

    private final int index;
    private final boolean append;
    private final E element;
    
    public ListAddAction(int index, boolean append, E element) {
        this.index = index;
        this.append = append;
        this.element = element;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doAction(C list, UpdateContext context, ViewToEntityMapper mapper, CollectionRemoveListener removeListener) {
        if (append) {
            if (mapper != null) {
                list.add((E) mapper.applyToEntity(context, null, element));
            } else {
                list.add(element);
            }
        } else {
            if (mapper != null) {
                list.add(index, (E) mapper.applyToEntity(context, null, element));
            } else {
                list.add(index, element);
            }
        }
    }

    @Override
    public void undo(C collection, Collection<?> removedObjects, Collection<?> addedObjects) {
        collection.remove(index);
    }

    @Override
    public boolean containsObject(C collection, Object o) {
        return o == element;
    }

    @Override
    public Collection<Object> getAddedObjects() {
        return Collections.<Object>singleton(element);
    }

    @Override
    public Collection<Object> getRemovedObjects() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getAddedObjects(C collection) {
        if (collection.contains(element)) {
            return Collections.emptyList();
        }
        return Collections.<Object>singleton(element);
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
            return Collections.<Map.Entry<Object, Integer>>singletonList(new AbstractMap.SimpleEntry<Object, Integer>(element, index));
        }
    }

    @Override
    public List<Map.Entry<Object, Integer>> getAppendedObjectEntries() {
        if (append) {
            return Collections.<Map.Entry<Object, Integer>>singletonList(new AbstractMap.SimpleEntry<Object, Integer>(element, index));
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
        if (element != oldElem) {
            return null;
        }
        return new ListAddAction(index, append, elem);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CollectionAction<C> replaceObjects(Map<Object, Object> objectMapping) {
        if (objectMapping == null) {
            return this;
        }
        Object newElement = objectMapping.get(element);

        if (newElement == null) {
            return this;
        }
        return new ListAddAction(index, append, newElement);
    }

    @Override
    public void addAction(RecordingCollection<?, ?> recordingCollection, List<CollectionAction<C>> actions) {
        actions.add(this);
    }

}
