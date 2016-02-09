package com.blazebit.persistence.view.impl.update.flush;

import javax.persistence.Query;

public interface DirtyAttributeFlusher<E, V> {

    public boolean supportsQueryFlush();
    
    public void flushQuery(Query query, V value);

    public void flushEntity(E entity, V value);
    
}
