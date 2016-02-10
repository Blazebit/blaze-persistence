package com.blazebit.persistence.view.impl.collection;

import java.util.List;

public class ListSetAction<C extends List<E>, E> implements ListAction<C> {

    private final int index;
    private final E element;
    
    public ListSetAction(int index, E element) {
        this.index = index;
        this.element = element;
    }

    @Override
    public void doAction(C list) {
        list.set(index, element);
    }

}
