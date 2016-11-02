/*
 * Copyright 2014 - 2016 Blazebit.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class RecordingCollection<C extends Collection<E>, E> implements Collection<E> {

    protected final C delegate;
    protected final List<CollectionAction<C>> actions;

    public RecordingCollection(C delegate) {
        this.delegate = delegate;
        this.actions = new ArrayList<CollectionAction<C>>();
    }
    
    public C getDelegate() {
        return delegate;
    }
    
    public boolean hasActions() {
        return actions.size() > 0;
    }
    
    public void clearActions() {
        actions.clear();
    }
    
    public void replay(C collection) {
        for (CollectionAction<C> action : actions) {
            action.doAction(collection);
        }
    }

    @Override
    public boolean add(E e) {
        actions.add(new CollectionAddAction<C, E>(e));
        return delegate.add(e);
    }

    @Override
    public boolean remove(Object o) {
        actions.add(new CollectionRemoveAction<C, E>(o));
        return delegate.remove(o);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        actions.add(new CollectionAddAllAction<C, E>(c));
        return delegate.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        actions.add(new CollectionRemoveAllAction<C, E>(c));
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        actions.add(new CollectionRetainAllAction<C, E>(c));
        return delegate.retainAll(c);
    }

    @Override
    public void clear() {
        actions.add(new CollectionClearAction<C, E>());
        delegate.clear();
    }

    @Override
    public Iterator<E> iterator() {
        return new RecordingIterator<Iterator<E>, E>(delegate.iterator());
    }
    
    /**************
     * Read-only
     *************/

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((delegate == null) ? 0 : delegate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }
}
