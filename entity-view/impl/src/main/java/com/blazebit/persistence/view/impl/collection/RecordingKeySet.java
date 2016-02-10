package com.blazebit.persistence.view.impl.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class RecordingKeySet<C extends Map<K, V>, K, V> implements Set<K> {

    protected final Set<K> delegate;
    protected final RecordingMap<C, K, V> recordingMap;

    public RecordingKeySet(Set<K> delegate, RecordingMap<C, K, V> recordingMap) {
        this.delegate = delegate;
        this.recordingMap = recordingMap;
    }

    @Override
    public boolean add(K e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        recordingMap.actions.add(new MapRemoveAction<C, K, V>(o));
        return delegate.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        recordingMap.actions.add(new MapRemoveAllKeysAction<C, K, V>(c));
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        recordingMap.actions.add(new MapRetainAllKeysAction<C, K, V>(c));
        return delegate.retainAll(c);
    }

    @Override
    public void clear() {
        recordingMap.actions.add(new MapClearAction<C, K, V>());
        delegate.clear();
    }

    @Override
    public Iterator<K> iterator() {
        return new RecordingIterator<Iterator<K>, K>(delegate.iterator());
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

}
