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
public class CollectionClearAction<C extends Collection<E>, E> implements CollectionAction<C> {

    @Override
    public void doAction(C collection, UpdateContext context, ViewToEntityMapper mapper, CollectionRemoveListener removeListener) {
        if (removeListener != null) {
            for (E e : collection) {
                removeListener.onCollectionRemove(context, e);
            }
        }
        collection.clear();
    }

    @Override
    public boolean containsObject(C collection, Object o) {
        // Trivially, a clear action contains all objects
        return true;
    }

    @Override
    public Collection<Object> getAddedObjects() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedObjects() {
        // The clear action is used internally only, so this shouldn't matter
        return null;
    }

    @Override
    public Collection<Object> getAddedObjects(C collection) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Object> getRemovedObjects(C collection) {
        return (Collection<Object>) collection;
    }

    @Override
    public CollectionAction<C> replaceObject(Object oldElem, Object elem) {
        return null;
    }

    @Override
    public void addAction(List<CollectionAction<C>> actions, Collection<Object> addedElements, Collection<Object> removedElements) {
        actions.clear();
        actions.add(this);
    }
}
