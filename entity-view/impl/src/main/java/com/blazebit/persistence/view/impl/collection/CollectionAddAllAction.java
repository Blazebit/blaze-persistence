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

import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CollectionAddAllAction<C extends Collection<E>, E> implements CollectionAction<C> {

    private final Collection<E> elements;

    public CollectionAddAllAction(int size, boolean allowDuplicates) {
        if (allowDuplicates) {
            this.elements = new ArrayList<>(size);
        } else {
            this.elements = Collections.newSetFromMap(new IdentityHashMap<E, Boolean>(size));
        }
    }

    public CollectionAddAllAction(E element, boolean allowDuplicates) {
        if (allowDuplicates) {
            this.elements = new ArrayList<>();
        } else {
            this.elements = Collections.newSetFromMap(new IdentityHashMap<E, Boolean>());
        }
        this.elements.add(element);
    }
    
    public CollectionAddAllAction(Collection<? extends E> collection, boolean allowDuplicates) {
        if (allowDuplicates) {
            this.elements = new ArrayList<>(collection);
        } else {
            this.elements = Collections.newSetFromMap(new IdentityHashMap<E, Boolean>(collection.size()));
            this.elements.addAll(collection);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doAction(C collection, UpdateContext context, ViewToEntityMapper mapper, CollectionRemoveListener removeListener) {
        if (mapper != null) {
            if (collection instanceof ArrayList<?>) {
                ((ArrayList<?>) collection).ensureCapacity(collection.size() + elements.size());
            }
            for (Object e : elements) {
                collection.add((E) mapper.applyToEntity(context, null, e));
            }
        } else {
            collection.addAll(elements);
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

    public boolean isEmpty() {
        return elements.isEmpty();
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
    @SuppressWarnings("unchecked")
    public CollectionAction<C> replaceObject(Object oldElem, Object elem) {
        List<Object> newElements = RecordingUtils.replaceElements(elements, oldElem, elem);

        if (newElements == null) {
            return null;
        }
        return new CollectionAddAllAction(newElements, elements instanceof List);
    }

    @Override
    public void addAction(List<CollectionAction<C>> actions, Collection<Object> addedElements, Collection<Object> removedElements) {
        CollectionOperations op = new CollectionOperations(actions);

        if (op.addElements(addedElements)) {
            actions.add(this);
        }

        op.removeEmpty();
    }

    public void add(Object o) {
        elements.add((E) o);
    }

    public Collection<Object> onRemoveObjects(Collection<Object> objectsToRemove) {
        return RecordingUtils.compensateObjects(elements, objectsToRemove);
    }

    public Collection<Object> onAddObjects(Collection<Object> objectsToAdd) {
        elements.addAll((Collection<? extends E>) (Collection<?>) objectsToAdd);
        return Collections.emptyList();
    }

}
