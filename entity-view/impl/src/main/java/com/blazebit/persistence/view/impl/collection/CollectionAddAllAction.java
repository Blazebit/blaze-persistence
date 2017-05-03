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
public class CollectionAddAllAction<C extends Collection<E>, E> implements CollectionAction<C> {

    private final List<? extends E> elements;
    
    public CollectionAddAllAction(Collection<? extends E> collection) {
        this.elements = new ArrayList<E>(collection);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doAction(C collection, UpdateContext context, ViewToEntityMapper mapper) {
        if (mapper != null) {
            if (collection instanceof ArrayList) {
                ((ArrayList) collection).ensureCapacity(collection.size() + elements.size());
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

    @Override
    public Collection<Object> getAddedObjects(C collection) {
        return (Collection<Object>) elements;
    }

    @Override
    public Collection<Object> getRemovedObjects(C collection) {
        return Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public CollectionAction<C> replaceObject(Object oldElem, Object elem) {
        List<Object> newElements = ActionUtils.replaceElements(elements, oldElem, elem);

        if (newElements == null) {
            return null;
        }
        return new CollectionAddAllAction(newElements);
    }

}
