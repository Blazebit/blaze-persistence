package com.blazebit.persistence.view.impl.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecordingMap<C extends Map<K, V>, K, V> implements Map<K, V> {

    protected final C delegate;
    protected final List<MapAction<C>> actions;

    public RecordingMap(C delegate) {
        this.delegate = delegate;
        this.actions = new ArrayList<MapAction<C>>();
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
    
    public void replay(C map) {
        for (MapAction<C> action : actions) {
            action.doAction(map);
        }
    }

    public V put(K key, V value) {
        actions.add(new MapPutAction<C, K, V>(key, value));
        return delegate.put(key, value);
    }

    public V remove(Object key) {
        actions.add(new MapRemoveAction<C, K, V>(key));
        return delegate.remove(key);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        actions.add(new MapPutAllAction<C, K, V>(m));
        delegate.putAll(m);
    }

    public void clear() {
        actions.add(new MapClearAction<C, K, V>());
        delegate.clear();
    }

    public Set<K> keySet() {
        return new RecordingKeySet<C, K, V>(delegate.keySet(), this);
    }

    public Collection<V> values() {
        return new RecordingValuesCollection<C, K, V>(delegate.values(), this);
    }

    public Set<java.util.Map.Entry<K, V>> entrySet() {
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
    
    /**
     * We use identity for equals-hashCode
     */

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}
