package com.blazebit.persistence.view.impl.collection;

import java.util.List;

public class ListRemoveAction<C extends List<E>, E> implements ListAction<C> {

    private final int index;
    
    public ListRemoveAction(int index) {
        this.index = index;
    }

    @Override
    public void doAction(C list) {
        list.remove(index);
    }

}
