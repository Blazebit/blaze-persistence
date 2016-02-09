package com.blazebit.persistence.view.impl.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListAddAllAction<C extends List<E>, E> implements ListAction<C> {

    private final int index;
    private final Collection<? extends E> elements;
    
    public ListAddAllAction(int index, Collection<? extends E> collection) {
        this.index = index;
        this.elements = new ArrayList<E>(collection);
    }

    @Override
    public void doAction(C list) {
        list.addAll(index, elements);
    }

}
