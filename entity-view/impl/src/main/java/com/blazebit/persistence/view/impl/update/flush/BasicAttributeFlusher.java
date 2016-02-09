package com.blazebit.persistence.view.impl.update.flush;

import javax.persistence.Query;

import com.blazebit.reflection.PropertyPathExpression;

public class BasicAttributeFlusher<E, V> implements DirtyAttributeFlusher<E, V> {

    private final String parameterName;
    private final PropertyPathExpression<E, V> propertyPath;
    
    public BasicAttributeFlusher(String parameterName, PropertyPathExpression<E, V> propertyPath) {
        this.parameterName = parameterName;
        this.propertyPath = propertyPath;
    }

    @Override
    public boolean supportsQueryFlush() {
        return true;
    }

    @Override
    public void flushQuery(Query query, V value) {
        query.setParameter(parameterName, value);
    }

    @Override
    public void flushEntity(E entity, V value) {
        propertyPath.setValue(entity, value);
    }
}
