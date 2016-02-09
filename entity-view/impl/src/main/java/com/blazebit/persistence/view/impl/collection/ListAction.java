package com.blazebit.persistence.view.impl.collection;

import java.util.List;

public interface ListAction<T extends List<?>> extends CollectionAction<T> {

    public void doAction(T list);
    
}
