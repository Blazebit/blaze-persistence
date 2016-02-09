package com.blazebit.persistence.view.impl.collection;

import java.util.ArrayList;
import java.util.Collection;

public class CollectionAddAllAction<C extends Collection<E>, E> implements CollectionAction<C> {

    private final Collection<? extends E> elements;
    
    public CollectionAddAllAction(Collection<? extends E> collection) {
        this.elements = new ArrayList<E>(collection);
    }

    @Override
    public void doAction(C collection) {
        collection.addAll(elements);
    }

}
