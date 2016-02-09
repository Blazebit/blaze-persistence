package com.blazebit.persistence.view.impl.collection;

import java.util.Map;

public class MapRemoveEntryAction<C extends Map<K, V>, K, V> implements MapAction<C> {

    private final Object entry;

    public MapRemoveEntryAction(Object entry) {
        this.entry = entry;
    }

    @Override
    public void doAction(C map) {
        map.entrySet().remove(entry);
    }

}
