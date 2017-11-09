/*
 * Copyright 2014 - 2017 Blazebit.
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
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CollectionRetainAllAction<C extends Collection<E>, E> implements CollectionAction<C> {

    private final List<Object> elements;
    
    public CollectionRetainAllAction(Collection<?> collection) {
        this.elements = new ArrayList<Object>(collection);
    }

    @Override
    public void doAction(C collection, UpdateContext context, ViewToEntityMapper mapper) {
        if (mapper != null) {
            List<Object> mappedElements = new ArrayList<>(elements.size());
            for (Object e : elements) {
                mappedElements.add(mapper.applyToEntity(context, null, e));
            }
            collection.retainAll(mappedElements);
        } else {
            collection.retainAll(elements);
        }
    }

    @Override
    public boolean containsObject(C collection, Object o) {
        // For completeness we implemented this, but actually this is never needed
        for (Object element : elements) {
            boolean contained = false;
            for (Object realElement : collection) {
                if (element == realElement) {
                    contained = true;
                    break;
                }
            }

            if (!contained && element == o) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public Collection<Object> getAddedObjects(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedObjects(C collection) {
        Collection<Object> removedObjects = null;

        for (Object o : collection) {
            if (!elements.contains(o)) {
                if (removedObjects == null) {
                    removedObjects = new ArrayList<>(collection.size());
                }
                removedObjects.add(o);
            }
        }

        if (removedObjects == null) {
            return Collections.emptyList();
        } else {
            return removedObjects;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public CollectionAction<C> replaceObject(Object oldElem, Object elem) {
        List<Object> newElements = RecordingUtils.replaceElements(elements, oldElem, elem);

        if (newElements == null) {
            return null;
        }
        return new CollectionRetainAllAction(newElements);
    }

    @Override
    public void addAction(List<CollectionAction<C>> actions, Collection<Object> addedElements, Collection<Object> removedElements) {
        CollectionOperations op = new CollectionOperations(actions);
        op.removeElements(removedElements);
        op.removeEmpty();
        if (elements.isEmpty()) {
            actions.clear();
            actions.add((CollectionAction<C>) (CollectionAction) new CollectionClearAction<>());
        } else {
            actions.add(this);
        }
    }

    public Collection<Object> onRemoveObjects(Collection<Object> objectsToRemove) {
        elements.removeAll(objectsToRemove);
        return objectsToRemove;
    }

    public Collection<Object> onAddObjects(Collection<Object> objectsToAdd) {
        elements.addAll(objectsToAdd);
        return objectsToAdd;
    }

}
