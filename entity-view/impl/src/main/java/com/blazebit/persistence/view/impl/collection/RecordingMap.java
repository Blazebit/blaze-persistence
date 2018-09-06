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

import com.blazebit.persistence.view.impl.entity.MapViewToEntityMapper;
import com.blazebit.persistence.view.impl.proxy.DirtyTracker;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.spi.type.BasicDirtyTracker;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
public class RecordingMap<C extends Map<K, V>, K, V> implements Map<K, V>, DirtyTracker {

    private static final long[] DIRTY_MARKER = new long[0];

    protected final C delegate;
    protected final Set<Class<?>> allowedSubtypes;
    protected final boolean updatable;
    private final boolean optimize;
    private final boolean hashBased;
    private final boolean ordered;
    private BasicDirtyTracker parent;
    private int parentIndex;
    private boolean dirty;
    private List<MapAction<C>> actions;
    private Map<K, K> addedKeys;
    private Map<K, K> removedKeys;
    private Map<V, V> addedElements;
    private Map<V, V> removedElements;
    // We remember the iterator so we can do a proper hash based collection replacement
    private transient RecordingEntrySetReplacingIterator<K, V> currentIterator;

    protected RecordingMap(C delegate, Set<Class<?>> allowedSubtypes, boolean updatable, boolean optimize, boolean hashBased, boolean ordered) {
        this.delegate = delegate;
        this.allowedSubtypes = allowedSubtypes;
        this.updatable = updatable;
        this.optimize = optimize;
        this.hashBased = hashBased;
        this.ordered = ordered;
    }

    public RecordingMap(C delegate, boolean ordered, Set<Class<?>> allowedSubtypes, boolean updatable, boolean optimize) {
        this.delegate = delegate;
        this.allowedSubtypes = allowedSubtypes;
        this.updatable = updatable;
        this.optimize = optimize;
        this.ordered = ordered;
        this.hashBased = true;
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

        for (Map.Entry<K, V> entry : delegate.entrySet()) {
            K key = entry.getKey();
            if (key instanceof BasicDirtyTracker) {
                ((BasicDirtyTracker) key).$$_setParent(this, 1);
            }

            V value = entry.getValue();
            if (value instanceof BasicDirtyTracker) {
                ((BasicDirtyTracker) value).$$_setParent(this, 2);
            }
        }
    }

    @Override
    public boolean $$_hasParent() {
        return parent != null;
    }

    @Override
    public void $$_unsetParent() {
        this.parentIndex = 0;
        this.parent = null;

        for (Map.Entry<K, V> entry : delegate.entrySet()) {
            K key = entry.getKey();
            if (key instanceof BasicDirtyTracker) {
                ((BasicDirtyTracker) key).$$_unsetParent();
            }

            V value = entry.getValue();
            if (value instanceof BasicDirtyTracker) {
                ((BasicDirtyTracker) value).$$_unsetParent();
            }
        }
    }

    public boolean isHashBased() {
        return hashBased;
    }

    public RecordingEntrySetReplacingIterator<K, V> getCurrentIterator() {
        return currentIterator;
    }

    public RecordingEntrySetReplacingIterator<K, V> recordingIterator() {
        if (currentIterator != null) {
            throw new IllegalStateException("Multiple concurrent invocations for recording iterator!");
        }
        return currentIterator = new RecordingEntrySetReplacingIterator<>(this);
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
    
    public void setActions(List<MapAction<C>> actions, Map<K, K> addedKeys, Map<K, K> removedKeys, Map<V, V> addedElements, Map<V, V> removedElements) {
        this.actions = actions;
        this.addedKeys = addedKeys;
        this.removedKeys = removedKeys;
        this.addedElements = addedElements;
        this.removedElements = removedElements;
        if (ordered) {
            List<Object> objects = new ArrayList<>(delegate.size() * 2);
            for (Map.Entry<K, V> entry : delegate.entrySet()) {
                K key = entry.getKey();
                V value = entry.getValue();
                for (K oldKey : addedKeys.keySet()) {
                    if (oldKey.equals(key) && key != oldKey) {
                        if (key instanceof DirtyTracker) {
                            ((DirtyTracker) key).$$_unsetParent();
                        }
                        if (oldKey instanceof DirtyTracker) {
                            ((DirtyTracker) oldKey).$$_setParent(this, 1);
                        }
                        key = oldKey;
                        break;
                    }
                }
                objects.add(key);
                for (V oldValue : addedElements.keySet()) {
                    if (oldValue.equals(value) && value != oldValue) {
                        if (value instanceof DirtyTracker) {
                            ((DirtyTracker) value).$$_unsetParent();
                        }
                        if (oldValue instanceof DirtyTracker) {
                            ((DirtyTracker) oldValue).$$_setParent(this, 2);
                        }
                        value = oldValue;
                        break;
                    }
                }
                objects.add(value);
            }
            delegate.clear();
            for (int i = 0; i < objects.size(); i += 2) {
                delegate.put((K) objects.get(i), (V) objects.get(i + 1));
            }
        } else {
            Iterator<Map.Entry<K, V>> iterator = delegate.entrySet().iterator();
            Map<K, V> newValues = new HashMap<>();
            while (iterator.hasNext()) {
                Map.Entry<K, V> entry = iterator.next();
                boolean removed = false;
                K oldKey = entry.getKey();
                for (K k : addedKeys.keySet()) {
                    if (k.equals(entry.getKey()) && entry.getKey() != k) {
                        if (oldKey instanceof DirtyTracker) {
                            ((DirtyTracker) oldKey).$$_unsetParent();
                        }
                        if (k instanceof DirtyTracker) {
                            ((DirtyTracker) k).$$_setParent(this, 1);
                        }
                        oldKey = k;
                        iterator.remove();
                        removed = true;
                        break;
                    }
                }
                V oldValue = entry.getValue();
                for (V v : addedElements.keySet()) {
                    if (v.equals(entry.getValue()) && entry.getValue() != v) {
                        if (oldValue instanceof DirtyTracker) {
                            ((DirtyTracker) oldValue).$$_unsetParent();
                        }
                        if (v instanceof DirtyTracker) {
                            ((DirtyTracker) v).$$_setParent(this, 2);
                        }
                        oldValue = v;
                        if (!removed) {
                            iterator.remove();
                            removed = true;
                        }
                        break;
                    }
                }
                if (removed) {
                    newValues.put(oldKey, oldValue);
                }
            }
            delegate.putAll(newValues);
        }
    }

    public List<MapAction<C>> resetActions(UpdateContext context) {
        List<MapAction<C>> oldActions = this.actions;
        Map<K, K> addedKeys = this.addedKeys;
        Map<K, K> removedKeys = this.removedKeys;
        Map<V, V> addedElements = this.addedElements;
        Map<V, V> removedElements = this.removedElements;
        this.actions = null;
        this.dirty = false;
        this.addedKeys = null;
        this.addedElements = null;
        this.removedKeys = null;
        this.removedElements = null;
        context.getInitialStateResetter().addRecordingMap(this, oldActions, addedKeys, removedKeys, addedElements, removedElements);
        return oldActions;
    }

    public void initiateActionsAgainstState(List<MapAction<C>> actions, C initialState) {
        Map<K, K> addedKeys = new IdentityHashMap<>();
        Map<K, K> removedKeys = new IdentityHashMap<>();
        Map<V, V> addedElements = new IdentityHashMap<>();
        Map<V, V> removedElements = new IdentityHashMap<>();

        for (MapAction<C> action : actions) {
            for (Object o : action.getAddedKeys(initialState)) {
                addedKeys.put((K) o, (K) o);
                removedKeys.remove(o);
                // We don't set the parent here because that will happen during the setParent call for this collection
            }
            for (Object o : action.getRemovedKeys(initialState)) {
                removedKeys.put((K) o, (K) o);
                addedKeys.remove(o);
                if (o instanceof BasicDirtyTracker) {
                    ((BasicDirtyTracker) o).$$_unsetParent();
                }
            }
            for (Object o : action.getAddedElements(initialState)) {
                addedElements.put((V) o, (V) o);
                removedElements.remove(o);
                // We don't set the parent here because that will happen during the setParent call for this collection
            }
            for (Object o : action.getRemovedElements(initialState)) {
                removedElements.put((V) o, (V) o);
                addedElements.remove(o);
                if (o instanceof BasicDirtyTracker) {
                    ((BasicDirtyTracker) o).$$_unsetParent();
                }
            }
        }

        this.actions = actions;
        this.dirty = true;
        this.addedKeys = addedKeys;
        this.addedElements = addedElements;
        this.removedKeys = removedKeys;
        this.removedElements = removedElements;
    }

    public List<MapAction<C>> getActions() {
        return actions;
    }

    public Set<K> getAddedKeys() {
        if (addedKeys == null) {
            return Collections.emptySet();
        }
        return addedKeys.keySet();
    }

    public Set<K> getRemovedKeys() {
        if (removedKeys == null) {
            return Collections.emptySet();
        }
        return removedKeys.keySet();
    }

    public Set<V> getAddedElements() {
        if (addedElements == null) {
            return Collections.emptySet();
        }
        return addedElements.keySet();
    }

    public Set<V> getRemovedElements() {
        if (removedElements == null) {
            return Collections.emptySet();
        }
        return removedElements.keySet();
    }

    public void replay(C map, UpdateContext context, MapViewToEntityMapper mapper, CollectionRemoveListener keyRemoveListener, CollectionRemoveListener valueRemoveListener) {
        if (actions != null) {
            for (MapAction<C> action : resetActions(context)) {
                action.doAction(map, context, mapper, keyRemoveListener, valueRemoveListener);
            }
        }
    }

    public void replaceActionElement(Object oldKey, Object oldValue, Object newKey, Object newValue) {
        if (actions != null && (oldKey != newKey || oldValue != newValue)) {
            ListIterator<MapAction<C>> iter = actions.listIterator();
            while (iter.hasNext()) {
                MapAction<C> action = iter.next();
                MapAction<C> newAction = action.replaceObject(oldKey, oldValue, newKey, newValue);
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

    protected void checkType(Map<?, ?> collection, String action) {
        if (collection != null && !allowedSubtypes.isEmpty()) {
            for (Object e : collection.values()) {
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

    protected final void addAction(MapAction<C> action) {
        if (!updatable) {
            throw new UnsupportedOperationException("Collection is not updatable. Only it's elements are mutable! Consider annotating @UpdatableMapping if you want the collection role to be updatable!");
        }
        Collection<Object> addedKeys = action.getAddedKeys();
        Collection<Object> removedKeys = action.getRemovedKeys();
        Collection<Object> addedElements = action.getAddedElements();
        Collection<Object> removedElements = action.getRemovedElements();
        if (this.actions == null) {
            this.actions = new ArrayList<>();
            this.addedKeys = new IdentityHashMap<>();
            this.addedElements = new IdentityHashMap<>();
            this.removedKeys = new IdentityHashMap<>();
            this.removedElements = new IdentityHashMap<>();
        }


        // addAction optimizes actions by figuring converting to physical changes
        if (optimize) {
            action.addAction(actions, addedKeys, removedKeys, addedElements, removedElements);
        } else {
            actions.add(action);
        }

        for (Object o : addedKeys) {
            // Only consider a key to be added if it hasn't been removed before
            if (this.removedKeys.remove(o) == null) {
                if (this.addedKeys.put((K) o, (K) o) == null) {
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
        for (Object o : removedKeys) {
            // Only consider a key to be removed if it hasn't been added before
            if (this.addedKeys.remove(o) == null) {
                if (this.removedKeys.put((K) o, (K) o) == null) {
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
        for (Object o : addedElements) {
            // Only consider an element to be added if it hasn't been removed before
            if (this.removedElements.remove(o) == null) {
                if (this.addedElements.put((V) o, (V) o) == null) {
                    if (parent != null && o instanceof BasicDirtyTracker) {
                        ((BasicDirtyTracker) o).$$_setParent(this, 2);
                    }
                }
            } else {
                if (parent != null && o instanceof BasicDirtyTracker) {
                    ((BasicDirtyTracker) o).$$_setParent(this, 2);
                }
            }
        }
        for (Object o : removedElements) {
            // Only consider an element to be removed if it hasn't been added before
            if (this.addedElements.remove(o) == null) {
                if (this.removedElements.put((V) o, (V) o) == null) {
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

    public V put(K key, V value) {
        checkType(value, "Putting");
        addAction(new MapPutAction<C, K, V>(key, value, delegate));
        return delegate.put(key, value);
    }

    void addRemoveAction(Object key) {
        addAction(new MapRemoveAction<C, K, V>(key, delegate));
    }

    public V remove(Object key) {
        addRemoveAction(key);
        return delegate.remove(key);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        checkType(m, "Putting");
        addAction(new MapPutAllAction<C, K, V>(m, delegate));
        delegate.putAll(m);
    }

    void addClearAction() {
        addAction(new MapRemoveAllKeysAction<C, K, V>(delegate.keySet(), delegate));
    }

    public void clear() {
        addClearAction();
        delegate.clear();
    }

    public Set<K> keySet() {
        return new RecordingKeySet<C, K, V>(delegate.keySet(), this);
    }

    public Collection<V> values() {
        return new RecordingValuesCollection<C, K, V>(delegate.values(), this);
    }

    public RecordingEntrySet<C, K, V> entrySet() {
        return new RecordingEntrySet<C, K, V>(delegate.entrySet(), this);
    }
    
    /**************
     * Read-only
     *************/

    public int size() {
        return delegate.size();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    public V get(Object key) {
        return delegate.get(key);
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
