package com.blazebit.persistence.view.impl.collection;

import java.util.Collection;

public class CollectionRemoveAction<C extends Collection<E>, E> implements CollectionAction<C> {

    private final Object element;
    
    public CollectionRemoveAction(Object element) {
        this.element = element;
    }

    @Override
    public void doAction(C collection) {
        collection.remove(element);
    }

}
