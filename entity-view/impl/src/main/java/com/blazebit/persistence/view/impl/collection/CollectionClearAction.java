package com.blazebit.persistence.view.impl.collection;

import java.util.Collection;

public class CollectionClearAction<C extends Collection<E>, E> implements CollectionAction<C> {

    @Override
    public void doAction(C collection) {
        collection.clear();
    }

}
