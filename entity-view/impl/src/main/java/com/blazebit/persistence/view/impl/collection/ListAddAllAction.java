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
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ListAddAllAction<C extends List<E>, E> implements ListAction<C> {

    private final int index;
    private final List<? extends E> elements;
    
    public ListAddAllAction(int index, Collection<? extends E> collection) {
        this.index = index;
        this.elements = new ArrayList<E>(collection);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doAction(C list, UpdateContext context, ViewToEntityMapper mapper, CollectionRemoveListener removeListener) {
        if (mapper != null) {
            if (list instanceof ArrayList<?>) {
                ((ArrayList<?>) list).ensureCapacity(list.size() + elements.size());
            }
            int i = index;
            for (Object e : elements) {
                list.add(i++, (E) mapper.applyToEntity(context, null, e));
            }
        } else {
            list.addAll(index, elements);
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
    @SuppressWarnings("unchecked")
    public CollectionAction<C> replaceObject(Object oldElem, Object elem) {
        List<Object> newElements = RecordingUtils.replaceElements(elements, oldElem, elem);

        if (newElements == null) {
            return null;
        }
        return new ListAddAllAction(index, newElements);
    }

    @Override
    public void addAction(List<CollectionAction<C>> actions, Collection<Object> addedElements, Collection<Object> removedElements) {
        actions.add(this);
    }

}
