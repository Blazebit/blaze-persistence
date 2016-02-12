package com.blazebit.persistence.view.impl.collection;

import java.util.Collection;

public interface CollectionAction<T extends Collection<?>> {

    public void doAction(T collection);
    
}
