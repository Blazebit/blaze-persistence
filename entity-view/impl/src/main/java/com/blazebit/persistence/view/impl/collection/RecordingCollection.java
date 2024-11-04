/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import com.blazebit.persistence.view.RecordingContainer;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.spi.type.DirtyTracker;
import com.blazebit.persistence.view.spi.type.MutableStateTrackable;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.spi.type.BasicDirtyTracker;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
public class RecordingCollection<C extends Collection<E>, E> implements RecordingContainer<C>, Collection<E>, DirtyTracker, Serializable {

    private static final long[] DIRTY_MARKER = new long[0];

    protected final C delegate;
    protected final Set<Class<?>> allowedSubtypes;
    protected final Set<Class<?>> parentRequiringUpdateSubtypes;
    protected final Set<Class<?>> parentRequiringCreateSubtypes;
    protected final boolean updatable;
    protected final boolean indexed;
    private final boolean ordered;
    private final boolean optimize;
    private final boolean hashBased;
    private final boolean strictCascadingCheck;
    private BasicDirtyTracker parent;
    private int parentIndex;
    private boolean dirty;
    private List<CollectionAction<C>> actions;
    private Map<E, E> addedElements;
    private Map<E, E> removedElements;
    // We remember the iterator so we can do a proper hash based collection replacement
    private transient RecordingReplacingIterator<E> currentIterator;

    protected RecordingCollection(C delegate, boolean indexed, boolean ordered, Set<Class<?>> allowedSubtypes, Set<Class<?>> parentRequiringUpdateSubtypes, Set<Class<?>> parentRequiringCreateSubtypes, boolean updatable, boolean optimize, boolean hashBased, boolean strictCascadingCheck) {
        this.delegate = delegate;
        this.allowedSubtypes = allowedSubtypes;
        this.parentRequiringUpdateSubtypes = parentRequiringUpdateSubtypes;
        this.parentRequiringCreateSubtypes = parentRequiringCreateSubtypes;
        this.updatable = updatable;
        this.indexed = indexed;
        this.ordered = ordered;
        this.optimize = optimize;
        this.hashBased = hashBased;
        this.strictCascadingCheck = strictCascadingCheck;
    }

    public RecordingCollection(C delegate, boolean indexed, boolean ordered, Set<Class<?>> allowedSubtypes, Set<Class<?>> parentRequiringUpdateSubtypes, Set<Class<?>> parentRequiringCreateSubtypes, boolean updatable, boolean optimize, boolean strictCascadingCheck) {
        this.delegate = delegate;
        this.allowedSubtypes = allowedSubtypes;
        this.parentRequiringUpdateSubtypes = parentRequiringUpdateSubtypes;
        this.parentRequiringCreateSubtypes = parentRequiringCreateSubtypes;
        this.updatable = updatable;
        this.indexed = indexed;
        this.ordered = ordered;
        this.optimize = optimize;
        this.strictCascadingCheck = strictCascadingCheck;
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

    @Override
    public void $$_replaceAttribute(Object oldObject, int attributeIndex, Object newObject) {
        if (oldObject instanceof MutableStateTrackable) {
            ((MutableStateTrackable) oldObject).$$_removeReadOnlyParent(this, attributeIndex);
        }
        if (newObject instanceof MutableStateTrackable) {
            ((MutableStateTrackable) newObject).$$_addReadOnlyParent(this, attributeIndex);
        }
        if (currentIterator != null && currentIterator.getCurrent() == oldObject) {
            // This happens while persisting
            return;
        }
        if (newObject == null) {
            delegate.remove(oldObject);
        } else {
            if (ordered) {
                List<E> newCollection = new ArrayList<>(delegate.size());
                for (E e : delegate) {
                    if (e == oldObject) {
                        newCollection.add((E) newObject);
                    } else {
                        newCollection.add(e);
                    }
                }
                delegate.clear();
                delegate.addAll(newCollection);
            } else {
                delegate.remove(oldObject);
                delegate.add((E) newObject);
            }
        }
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

    @Override
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

    void addAddedElement(Object o) {
        if (removedElements.remove(o) == null) {
            addedElements.put((E) o, (E) o);
        }
        if (parent != null && o instanceof BasicDirtyTracker) {
            ((BasicDirtyTracker) o).$$_setParent(this, 1);
        }
    }

    void addRemovedElement(Object o) {
        if (addedElements.remove(o) == null) {
            removedElements.put((E) o, (E) o);
        }
        if (o instanceof BasicDirtyTracker) {
            ((BasicDirtyTracker) o).$$_unsetParent();
        }
    }

    public void setActions(RecordingCollection<C, E> recordingCollection, Map<Object, Object> objectMapping) {
        if (recordingCollection.actions == null) {
            this.actions = null;
            this.addedElements = null;
            this.removedElements = null;
        } else {
            this.actions = new ArrayList<>(recordingCollection.actions.size());
            this.addedElements = new IdentityHashMap<>(recordingCollection.addedElements.size());
            this.removedElements = new IdentityHashMap<>(recordingCollection.removedElements.size());

            for (CollectionAction<C> action : recordingCollection.actions) {
                actions.add(action.replaceObjects(objectMapping));
            }

            for (E e : recordingCollection.addedElements.keySet()) {
                E newElement = (E) objectMapping.get(e);
                if (newElement == null) {
                    addedElements.put(e, e);
                } else {
                    addedElements.put(newElement, newElement);
                }
            }

            for (E e : recordingCollection.removedElements.keySet()) {
                E newElement = (E) objectMapping.get(e);
                if (newElement == null) {
                    removedElements.put(e, e);
                } else {
                    removedElements.put(newElement, newElement);
                }
            }
        }
        if (recordingCollection.dirty) {
            $$_markDirty(-1);
        }
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
                            ((DirtyTracker) elem).$$_unsetParent();
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
                            ((DirtyTracker) elem).$$_unsetParent();
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
        $$_markDirty(-1);
    }

    protected C copyDelegate() {
        if (ordered) {
            if (hashBased) {
                return (C) new LinkedHashSet<>(delegate);
            } else {
                return (C) new ArrayList<>(delegate);
            }
        } else {
            if (hashBased) {
                return (C) new HashSet<>(delegate);
            } else {
                return (C) new ArrayList<>(delegate);
            }
        }
    }

    public C getInitialVersion() {
        if (actions == null || actions.isEmpty()) {
            return (C) this;
        }
        C collection = copyDelegate();
        for (int i = actions.size() - 1; i >= 0; i--) {
            CollectionAction<C> action = actions.get(i);
            action.undo(collection, removedElements.keySet(), addedElements.keySet());
        }
        return collection;
    }

    public List<CollectionAction<C>> resetActions(UpdateContext context) {
        List<CollectionAction<C>> oldActions = this.actions;
        if (oldActions == null) {
            return Collections.emptyList();
        }
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
            // Specially handle the clear action by considering the initial state as fully removed
            if (action instanceof CollectionClearAction<?, ?>) {
                for (E o : initialState) {
                    removedElements.put(o, o);
                }
            } else {
                for (Object o : action.getAddedObjects()) {
                    if (removedElements.remove(o) == null) {
                        addedElements.put((E) o, (E) o);
                        // We don't set the parent here because that will happen during the setParent call for this collection
                    }
                }
                for (Object o : action.getRemovedObjects()) {
                    if (addedElements.remove(o) == null) {
                        removedElements.put((E) o, (E) o);
                    }
                }
            }
        }

        this.actions = actions;
        this.dirty = true;
        this.addedElements = addedElements;
        this.removedElements = removedElements;

        for (E o : removedElements.keySet()) {
            if (o instanceof BasicDirtyTracker) {
                ((BasicDirtyTracker) o).$$_unsetParent();
            }
        }
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
        // We don't consider re-adds to sets to be actual changes
        if (removedElements.isEmpty() && !addedElements.isEmpty() && !allowDuplicates() && addedElements.removeAll(delegate) && addedElements.isEmpty()) {
            return;
        }

        if (this.actions == null) {
            this.actions = new ArrayList<>();
            this.addedElements = new IdentityHashMap<>();
            this.removedElements = new IdentityHashMap<>();
        }

        // addAction optimizes actions by figuring converting to physical changes
        if (optimize) {
            action.addAction(this, actions);
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
            boolean isNew;
            if (e instanceof EntityViewProxy) {
                c = ((EntityViewProxy) e).$$_getEntityViewClass();
                isNew = ((EntityViewProxy) e).$$_isNew();
            } else {
                c = e.getClass();
                isNew = false;
            }

            if (!allowedSubtypes.contains(c)) {
                throw new IllegalArgumentException(action + " instances of type [" + c.getName() + "] is not allowed!");
            }
            if (strictCascadingCheck) {
                if (e != parent && !isNew && parentRequiringUpdateSubtypes.contains(c) && !((DirtyTracker) e).$$_hasParent()) {
                    throw new IllegalArgumentException(action + " instances of type [" + c.getName() + "] is not allowed until they are assigned to an attribute that update cascades the type! " +
                            "If you want this attribute to cascade, annotate it with @UpdatableMapping(cascade = { UPDATE }). " +
                            "You can also turn off strict cascading checks by setting ConfigurationProperties.UPDATER_STRICT_CASCADING_CHECK to false");
                }
                if (e != parent && isNew && parentRequiringCreateSubtypes.contains(c) && !((DirtyTracker) e).$$_hasParent()) {
                    throw new IllegalArgumentException(action + " instances of type [" + c.getName() + "] is not allowed until they are assigned to an attribute that persist cascades the type! " +
                            "If you want this attribute to cascade, annotate it with @UpdatableMapping(cascade = { PERSIST }). " +
                            "You can also turn off strict cascading checks by setting ConfigurationProperties.UPDATER_STRICT_CASCADING_CHECK to false");
                }
            }
        }
    }

    protected void checkType(Collection<?> collection, String action) {
        if (collection != null && !collection.isEmpty() && !allowedSubtypes.isEmpty()) {
            for (Object e : collection) {
                Class<?> c;
                boolean isNew;
                if (e instanceof EntityViewProxy) {
                    c = ((EntityViewProxy) e).$$_getEntityViewClass();
                    isNew = ((EntityViewProxy) e).$$_isNew();
                } else {
                    c = e.getClass();
                    isNew = false;
                }

                if (!allowedSubtypes.contains(c)) {
                    throw new IllegalArgumentException(action + " instances of type [" + c.getName() + "] is not allowed!");
                }
                if (strictCascadingCheck) {
                    if (!isNew && parentRequiringUpdateSubtypes.contains(c) && !((DirtyTracker) e).$$_hasParent()) {
                        throw new IllegalArgumentException(action + " instances of type [" + c.getName() + "] is not allowed until they are assigned to an attribute that update cascades the type! If you want this attribute to cascade, annotate it with @UpdatableMapping(cascade = { UPDATE })");
                    }
                    if (isNew && parentRequiringCreateSubtypes.contains(c) && !((DirtyTracker) e).$$_hasParent()) {
                        throw new IllegalArgumentException(action + " instances of type [" + c.getName() + "] is not allowed until they are assigned to an attribute that persist cascades the type! If you want this attribute to cascade, annotate it with @UpdatableMapping(cascade = { PERSIST })");
                    }
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
        if (delegate.remove(o)) {
            addRemoveAction(o);
            return true;
        }
        return false;
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
        addAction(new CollectionRemoveAllAction<C, E>(c, allowDuplicates()));
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        addAction(CollectionRemoveAllAction.retainAll(c, delegate, allowDuplicates()));
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

    @Override
    public String toString() {
        return delegate.toString();
    }
}
