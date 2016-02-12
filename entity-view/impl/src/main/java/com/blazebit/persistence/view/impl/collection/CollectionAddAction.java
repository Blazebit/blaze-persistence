package com.blazebit.persistence.view.impl.collection;

import java.util.Collection;

public class CollectionAddAction<C extends Collection<E>, E> implements CollectionAction<C> {

    private final E element;
    
    public CollectionAddAction(E element) {
        this.element = element;
    }

    @Override
    public void doAction(C collection) {
        collection.add(element);
    }

}
