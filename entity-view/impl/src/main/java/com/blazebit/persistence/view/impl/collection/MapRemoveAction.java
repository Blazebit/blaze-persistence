package com.blazebit.persistence.view.impl.collection;

import java.util.Map;

public class MapRemoveAction<C extends Map<K, V>, K, V> implements MapAction<C> {

    private final Object key;

    public MapRemoveAction(Object key) {
        this.key = key;
    }

    @Override
    public void doAction(C map) {
        map.remove(key);
    }

}
