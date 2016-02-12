package com.blazebit.persistence.view.impl.update.flush;

import java.util.Collection;

import javax.persistence.Query;

import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.reflection.PropertyPathExpression;

public class CollectionAttributeFlusher<E, V extends Collection<?>> implements DirtyAttributeFlusher<E, V> {

    private final PropertyPathExpression<E, Collection<?>> propertyPath;
    
    @SuppressWarnings("unchecked")
    public CollectionAttributeFlusher(PropertyPathExpression<E, ? extends Collection<?>> propertyPath) {
        this.propertyPath = (PropertyPathExpression<E, Collection<?>>) propertyPath;
    }

    @Override
    public boolean supportsQueryFlush() {
        return false;
    }

    @Override
    public void flushQuery(Query query, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void flushEntity(E entity, V value) {
        if (value instanceof RecordingCollection<?, ?>) {
            ((RecordingCollection<Collection<?>, ?>) value).replay(propertyPath.getValue(entity));
        } else {
            propertyPath.setValue(entity, value);
        }
    }
}
