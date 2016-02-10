package com.blazebit.persistence.view.impl.collection;

import java.util.Map;

public interface MapAction<T extends Map<?, ?>> {

    public void doAction(T map);
    
}
