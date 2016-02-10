package com.blazebit.persistence.view.impl.collection;

import java.util.Map;

public class MapRemoveValueAction<C extends Map<K, V>, K, V> implements MapAction<C> {

    private final Object value;

    public MapRemoveValueAction(Object value) {
        this.value = value;
    }

    @Override
    public void doAction(C map) {
        map.values().remove(value);
    }

}
