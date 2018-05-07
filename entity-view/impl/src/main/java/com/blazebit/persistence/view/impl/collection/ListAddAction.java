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
public class ListAddAction<C extends List<E>, E> implements ListAction<C> {

    private final int index;
    private final E element;
    
    public ListAddAction(int index, E element) {
        this.index = index;
        this.element = element;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doAction(C list, UpdateContext context, ViewToEntityMapper mapper, CollectionRemoveListener removeListener) {
        if (mapper != null) {
            list.add(index, (E) mapper.applyToEntity(context, null, element));
        } else {
            list.add(index, element);
        }
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
    @SuppressWarnings("unchecked")
    public CollectionAction<C> replaceObject(Object oldElem, Object elem) {
        if (element != oldElem) {
            return null;
        }
        return new ListAddAction(index, elem);
    }

    @Override
    public void addAction(List<CollectionAction<C>> actions, Collection<Object> addedElements, Collection<Object> removedElements) {
        actions.add(this);
    }

}
