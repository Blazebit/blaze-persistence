package com.blazebit.persistence.view.impl.update.flush;

import java.util.Map;

import javax.persistence.Query;

import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.reflection.PropertyPathExpression;

public class MapAttributeFlusher<E, V extends Map<?, ?>> implements DirtyAttributeFlusher<E, V> {

    private final PropertyPathExpression<E, Map<?, ?>> propertyPath;

    @SuppressWarnings("unchecked")
    public MapAttributeFlusher(PropertyPathExpression<E, ? extends Map<?, ?>> propertyPath) {
        this.propertyPath = (PropertyPathExpression<E, Map<?, ?>>) propertyPath;
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
        if (value instanceof RecordingMap<?, ?, ?>) {
            ((RecordingMap<Map<?, ?>, ?, ?>) value).replay(propertyPath.getValue(entity));
        } else {
            propertyPath.setValue(entity, value);
        }
    }
}
