package com.blazebit.persistence.view.impl.collection;

import java.util.Set;

public class RecordingSet<C extends Set<E>, E> extends RecordingCollection<C, E> implements Set<E> {

    public RecordingSet(C delegate) {
        super(delegate);
    }

}
