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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ListSetAction<C extends List<E>, E> implements ListAction<C> {

    final int index;
    final E element;
    final E removedElementInView;
    
    public ListSetAction(int index, E element, List<?> delegate) {
        this.index = index;
        this.element = element;
        this.removedElementInView = (E) delegate.get(index);
    }

    private ListSetAction(int index, E element, E removedElementInView) {
        this.index = index;
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
    public boolean containsObject(C collection, Object o) {
        return element == o;
    }

    @Override
    public Collection<Object> getAddedObjects() {
        return Collections.<Object>singleton(element);
    }

    @Override
    public Collection<Object> getRemovedObjects() {
        return Collections.<Object>singleton(removedElementInView);
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
    @SuppressWarnings("unchecked")
    public CollectionAction<C> replaceObject(Object oldElem, Object elem) {
        if (element != oldElem) {
            return null;
        }
        return new ListSetAction(index, elem, removedElementInView);
    }

    @Override
    public void addAction(List<CollectionAction<C>> actions, Collection<Object> addedElements, Collection<Object> removedElements) {
        CollectionAction<C> lastAction;
        if (!actions.isEmpty() && (lastAction = actions.get(actions.size() - 1)) instanceof ListSetAction<?, ?>) {
            if (index == ((ListSetAction<?, ?>) lastAction).index) {
                actions.set(actions.size() - 1, this);
                return;
            }
        }
        actions.add(this);
    }

}
