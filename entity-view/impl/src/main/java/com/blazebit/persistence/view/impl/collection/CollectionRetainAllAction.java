package com.blazebit.persistence.view.impl.collection;

import java.util.ArrayList;
import java.util.Collection;

public class CollectionRetainAllAction<C extends Collection<E>, E> implements CollectionAction<C> {

    private final Collection<?> elements;
    
    public CollectionRetainAllAction(Collection<?> collection) {
        this.elements = new ArrayList<Object>(collection);
    }

    @Override
    public void doAction(C collection) {
        collection.retainAll(elements);
    }

}
