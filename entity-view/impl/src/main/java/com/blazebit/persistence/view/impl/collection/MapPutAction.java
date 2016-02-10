package com.blazebit.persistence.view.impl.collection;

import java.util.Map;

public class MapPutAction<C extends Map<K, V>, K, V> implements MapAction<C> {

    private final K key;
    private final V value;

    public MapPutAction(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public void doAction(C map) {
        map.put(key, value);
    }

}
