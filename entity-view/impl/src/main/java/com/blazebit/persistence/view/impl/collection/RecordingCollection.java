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

import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.proxy.DirtyTracker;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.spi.type.BasicDirtyTracker;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@SuppressWarnings("checkstyle:methodname")
public class RecordingCollection<C extends Collection<E>, E> implements Collection<E>, DirtyTracker {

    private static final long[] DIRTY_MARKER = new long[0];

    protected final C delegate;
    protected final Set<Class<?>> allowedSubtypes;
    protected final boolean updatable;
    protected final boolean indexed;
    private final boolean ordered;
    private final boolean optimize;
    private final boolean hashBased;
    private BasicDirtyTracker parent;
    private int parentIndex;
    private boolean dirty;
    private List<CollectionAction<C>> actions;
    private Map<E, E> addedElements;
    private Map<E, E> removedElements;
    // We remember the iterator so we can do a proper hash based collection replacement
    private transient RecordingReplacingIterator<E> currentIterator;

    protected RecordingCollection(C delegate, boolean indexed, boolean ordered, Set<Class<?>> allowedSubtypes, boolean updatable, boolean optimize, boolean hashBased) {
        this.delegate = delegate;
        this.allowedSubtypes = allowedSubtypes;
        this.updatable = updatable;
        this.indexed = indexed;
        this.ordered = ordered;
        this.optimize = optimize;
        this.hashBased = hashBased;
    }

    public RecordingCollection(C delegate, boolean indexed, boolean ordered, Set<Class<?>> allowedSubtypes, boolean updatable, boolean optimize) {
        this.delegate = delegate;
        this.allowedSubtypes = allowedSubtypes;
        this.updatable = updatable;
        this.indexed = indexed;
        this.ordered = ordered;
        this.optimize = optimize;
        this.hashBased = false;
    }

    @Override
    public boolean $$_isDirty() {
        return dirty;
    }

    @Override
    public boolean $$_isDirty(int attributeIndex) {
        return dirty;
    }

    @Override
    public <T> boolean $$_copyDirty(T[] source, T[] target) {
        if (dirty) {
            target[0] = source[0];
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void $$_setDirty(long[] dirty) {
        if (dirty == null) {
            this.dirty = false;
        } else {
            this.dirty = true;
        }
    }

    @Override
    public long[] $$_resetDirty() {
        if (dirty) {
            dirty = false;
            return DIRTY_MARKER;
        } else {
            return null;
        }
    }

    @Override
    public long[] $$_getDirty() {
        if (dirty) {
            return DIRTY_MARKER;
        } else {
            return null;
        }
    }

    @Override
    public long $$_getSimpleDirty() {
        if (dirty) {
            return 1L;
        } else {
            return 0;
        }
    }

    @Override
    public void $$_markDirty(int attributeIndex) {
        this.dirty = true;
        if (parent != null) {
            parent.$$_markDirty(this.parentIndex);
        }
    }

    @Override
    public void $$_unmarkDirty() {
        dirty = false;
    }

    @Override
    public void $$_setParent(BasicDirtyTracker parent, int parentIndex) {
        if (this.parent != null) {
            throw new IllegalStateException("Parent object for " + this.toString() + " is already set to " + this.parent.toString() + " and can't be set to:" + parent.toString());
        }
        this.parent = parent;
        this.parentIndex = parentIndex;
        for (E e : delegate) {
            if (e instanceof BasicDirtyTracker) {
                ((BasicDirtyTracker) e).$$_setParent(this, 1);
            }
        }
    }

    @Override
    public boolean $$_hasParent() {
        return parent != null;
    }

    public void $$_unsetParent() {
        this.parentIndex = 0;
        this.parent = null;

        for (E e : delegate) {
            if (e instanceof BasicDirtyTracker) {
                ((BasicDirtyTracker) e).$$_unsetParent();
            }
        }
    }

    public boolean isHashBased() {
        return hashBased;
    }

    public RecordingReplacingIterator<E> getCurrentIterator() {
        return currentIterator;
    }

    public RecordingReplacingIterator<E> recordingIterator() {
        if (currentIterator != null) {
            throw new IllegalStateException("Multiple concurrent invocations for recording iterator!");
        }
        return currentIterator = new RecordingReplacingIterator<>(this);
    }

    public void resetRecordingIterator() {
        if (currentIterator == null) {
            throw new IllegalStateException("Multiple concurrent invocations for recording iterator!");
        }
        currentIterator.reset();
        currentIterator = null;
    }

    public C getDelegate() {
        return delegate;
    }
    
    public boolean hasActions() {
        return actions != null && actions.size() > 0;
    }

    public List<CollectionAction<C>> getActions() {
        return actions;
    }

    public Set<E> getAddedElements() {
        if (addedElements == null) {
            return Collections.emptySet();
        }
        return addedElements.keySet();
    }

    public Set<E> getRemovedElements() {
        if (removedElements == null) {
            return Collections.emptySet();
        }
        return removedElements.keySet();
    }

    public void setActions(List<CollectionAction<C>> actions, Map<E, E> addedElements, Map<E, E> removedElements) {
        this.actions = actions;
        this.addedElements = addedElements;
        this.removedElements = removedElements;
        if (ordered) {
            List<E> objects = new ArrayList<>(delegate.size());
            for (E elem : delegate) {
                for (E oldElem : addedElements.keySet()) {
                    if (oldElem.equals(elem) && elem != oldElem) {
                        if (elem instanceof DirtyTracker) {
                            ((DirtyTracker) oldElem).$$_unsetParent();
                        }
                        if (oldElem instanceof DirtyTracker) {
                            ((DirtyTracker) oldElem).$$_setParent(this, 1);
                        }
                        elem = oldElem;
                        break;
                    }
                }
                objects.add(elem);
            }
            delegate.clear();
            delegate.addAll(objects);
        } else {
            Iterator<E> iterator = delegate.iterator();
            while (iterator.hasNext()) {
                E elem = iterator.next();
                for (E oldElem : addedElements.keySet()) {
                    if (oldElem.equals(elem) && elem != oldElem) {
                        if (elem instanceof DirtyTracker) {
                            ((DirtyTracker) oldElem).$$_unsetParent();
                        }
                        if (oldElem instanceof DirtyTracker) {
                            ((DirtyTracker) oldElem).$$_setParent(this, 1);
                        }
                        iterator.remove();
                        break;
                    }
                }
            }
            delegate.addAll(addedElements.keySet());
        }
    }

    public List<CollectionAction<C>> resetActions(UpdateContext context) {
        List<CollectionAction<C>> oldActions = this.actions;
        Map<E, E> addedElements = this.addedElements;
        Map<E, E> removedElements = this.removedElements;
        this.actions = null;
        this.dirty = false;
        this.addedElements = null;
        this.removedElements = null;
        context.getInitialStateResetter().addRecordingCollection(this, oldActions, addedElements, removedElements);
        return oldActions;
    }

    public void initiateActionsAgainstState(List<CollectionAction<C>> actions, C initialState) {
        Map<E, E> addedElements = new IdentityHashMap<>();
        Map<E, E> removedElements = new IdentityHashMap<>();

        for (CollectionAction<C> action : actions) {
            for (Object o : action.getAddedObjects(initialState)) {
                addedElements.put((E) o, (E) o);
                removedElements.remove(o);
                // We don't set the parent here because that will happen during the setParent call for this collection
            }
            for (Object o : action.getRemovedObjects(initialState)) {
                removedElements.put((E) o, (E) o);
                addedElements.remove(o);
                if (o instanceof BasicDirtyTracker) {
                    ((BasicDirtyTracker) o).$$_unsetParent();
                }
            }
        }

        this.actions = actions;
        this.dirty = true;
        this.addedElements = addedElements;
        this.removedElements = removedElements;
    }

    protected boolean allowDuplicates() {
        return true;
    }

    protected final void addAction(CollectionAction<C> action) {
        if (!updatable) {
            throw new UnsupportedOperationException("Collection is not updatable. Only it's elements are mutable! Consider annotating @UpdatableMapping if you want the collection role to be updatable!");
        }
        Collection<Object> addedElements = action.getAddedObjects();
        Collection<Object> removedElements = action.getRemovedObjects();
        if (this.actions == null) {
            this.actions = new ArrayList<>();
            this.addedElements = new IdentityHashMap<>();
            this.removedElements = new IdentityHashMap<>();
        }

        // addAction optimizes actions by figuring converting to physical changes
        if (optimize) {
            action.addAction(actions, addedElements, removedElements);
        } else {
            actions.add(action);
        }

        for (Object o : addedElements) {
            // Only consider an element to be added if it hasn't been removed before
            if (this.removedElements.remove(o) == null) {
                if (this.addedElements.put((E) o, (E) o) == null) {
                    if (parent != null && o instanceof BasicDirtyTracker) {
                        ((BasicDirtyTracker) o).$$_setParent(this, 1);
                    }
                }
            } else {
                if (parent != null && o instanceof BasicDirtyTracker) {
                    ((BasicDirtyTracker) o).$$_setParent(this, 1);
                }
            }
        }
        for (Object o : removedElements) {
            // Only consider an element to be removed if it hasn't been added before
            if (this.addedElements.remove(o) == null) {
                if (this.removedElements.put((E) o, (E) o) == null) {
                    if (o instanceof BasicDirtyTracker) {
                        ((BasicDirtyTracker) o).$$_unsetParent();
                    }
                }
            } else {
                if (o instanceof BasicDirtyTracker) {
                    ((BasicDirtyTracker) o).$$_unsetParent();
                }
            }
        }
        $$_markDirty(-1);
    }
    
    public void replay(C collection, UpdateContext context, ViewToEntityMapper mapper, CollectionRemoveListener removeListener) {
        if (actions != null) {
            for (CollectionAction<C> action : resetActions(context)) {
                action.doAction(collection, context, mapper, removeListener);
            }
        }
    }

    public void replaceActionElement(Object oldElem, Object elem) {
        if (actions != null && oldElem != elem) {
            ListIterator<CollectionAction<C>> iter = actions.listIterator();
            while (iter.hasNext()) {
                CollectionAction<C> action = iter.next();
                CollectionAction<C> newAction = action.replaceObject(oldElem, elem);
                if (newAction != null) {
                    iter.set(newAction);
                }
            }
        }
    }

    protected void checkType(Object e, String action) {
        if (e != null && !allowedSubtypes.isEmpty()) {
            Class<?> c;
            if (e instanceof EntityViewProxy) {
                c = ((EntityViewProxy) e).$$_getEntityViewClass();
            } else {
                c = e.getClass();
            }

            if (!allowedSubtypes.contains(c)) {
                throw new IllegalArgumentException(action + " instances of type [" + c.getName() + "] is not allowed!");
            }
        }
    }

    protected void checkType(Collection<?> collection, String action) {
        if (collection != null && !allowedSubtypes.isEmpty()) {
            for (Object e : collection) {
                Class<?> c;
                if (e instanceof EntityViewProxy) {
                    c = ((EntityViewProxy) e).$$_getEntityViewClass();
                } else {
                    c = e.getClass();
                }

                if (!allowedSubtypes.contains(c)) {
                    throw new IllegalArgumentException(action + " instances of type [" + c.getName() + "] is not allowed!");
                }
            }
        }
    }

    void addAddAction(E e) {
        addAction(new CollectionAddAllAction<C, E>(e, allowDuplicates()));
    }

    @Override
    public boolean add(E e) {
        checkType(e, "Adding");
        addAddAction(e);
        return delegate.add(e);
    }

    void addRemoveAction(Object o) {
        addAction(new CollectionRemoveAllAction<C, E>(o, allowDuplicates()));
    }

    @Override
    public boolean remove(Object o) {
        checkType(o, "Removing");
        addRemoveAction(o);
        return delegate.remove(o);
    }

    void addAddAllAction(Collection<? extends E> c) {
        addAction(new CollectionAddAllAction<C, E>(c, allowDuplicates()));
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        checkType(c, "Adding");
        addAddAllAction(c);
        return delegate.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        checkType(c, "Removing");
        addAction(new CollectionRemoveAllAction<C, E>(c, allowDuplicates()));
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        addAction(new CollectionRetainAllAction<C, E>(c, delegate));
        return delegate.retainAll(c);
    }

    @Override
    public void clear() {
        addAction(new CollectionRemoveAllAction<C, E>(delegate, allowDuplicates()));
        delegate.clear();
    }

    @Override
    public Iterator<E> iterator() {
        return new RecordingIterator<>(this);
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
