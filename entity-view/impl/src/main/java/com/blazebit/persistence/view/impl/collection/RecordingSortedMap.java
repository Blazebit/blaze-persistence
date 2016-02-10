package com.blazebit.persistence.view.impl.collection;

import java.util.Comparator;
import java.util.SortedMap;

public class RecordingSortedMap<C extends SortedMap<K, V>, K, V> extends RecordingMap<C, K, V> implements SortedMap<K, V> {

    public RecordingSortedMap(C delegate) {
        super(delegate);
    }

    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        // TODO: implement
        throw new UnsupportedOperationException("Submaps for entity view collections are not yet supported!");
    }

    @Override
    public SortedMap<K, V> headMap(K toKey) {
        // TODO: implement
        throw new UnsupportedOperationException("Submaps for entity view collections are not yet supported!");
    }

    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        // TODO: implement
        throw new UnsupportedOperationException("Submaps for entity view collections are not yet supported!");
    }

    
    /**************
     * Read-only
     *************/

    @Override
    public Comparator<? super K> comparator() {
        return delegate.comparator();
    }

    @Override
    public K firstKey() {
        return delegate.firstKey();
    }

    @Override
    public K lastKey() {
        return delegate.lastKey();
    }

}
