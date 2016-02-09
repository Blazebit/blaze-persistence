package com.blazebit.persistence.view.impl.collection;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapPutAllAction<C extends Map<K, V>, K, V> implements MapAction<C> {

    private final Map<? extends K, ? extends V> elements;
    
    public MapPutAllAction(Map<? extends K, ? extends V> map) {
        this.elements = new LinkedHashMap<K, V>(map);
    }

    @Override
    public void doAction(C map) {
        map.putAll(elements);
    }

}
