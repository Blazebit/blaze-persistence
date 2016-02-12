package com.blazebit.persistence.view.impl.collection;

import java.util.Collection;
import java.util.Map;

public class MapRetainAllKeysAction<C extends Map<K, V>, K, V> implements MapAction<C> {

    private final Collection<?> elements;

    public MapRetainAllKeysAction(Collection<?> elements) {
        this.elements = elements;
    }

    @Override
    public void doAction(C map) {
        map.keySet().retainAll(elements);
    }

}
