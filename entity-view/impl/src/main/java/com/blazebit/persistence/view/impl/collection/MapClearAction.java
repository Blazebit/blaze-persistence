package com.blazebit.persistence.view.impl.collection;

import java.util.Map;

public class MapClearAction<C extends Map<K, V>, K, V> implements MapAction<C> {

    @Override
    public void doAction(C map) {
        map.clear();
    }

}
