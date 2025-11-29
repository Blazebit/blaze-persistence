/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.query;

import com.blazebit.persistence.impl.ParameterValueTransformer;
import com.blazebit.persistence.impl.ValuesParameterBinder;

import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.ParameterExpression;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CustomSQLQuery extends AbstractCustomQuery<Object> {

    private final Query delegate;

    public CustomSQLQuery(QuerySpecification querySpecification, Query delegate, Map<ParameterExpression<?>, String> criteriaNameMapping, Map<String, ParameterValueTransformer> transformers, Map<String, String> valuesParameters, Map<String, ValuesParameterBinder> valuesBinders) {
        super(querySpecification, criteriaNameMapping, transformers, valuesParameters, valuesBinders);
        this.delegate = delegate;
    }

    @Override
    public Query getDelegate() {
        return delegate;
    }

    public Map<String, String> getAddedCtes() {
        return querySpecification.getAddedCtes();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List getResultList() {
        throw new IllegalArgumentException("Can not call getResultList on a modification query!");
    }

    @Override
    public Object getSingleResult() {
        throw new IllegalArgumentException("Can not call getSingleResult on a modification query!");
    }

    @Override
    public Object getSingleResultOrNull() {
        throw new IllegalArgumentException("Can not call getSingleResultOrNull on a modification query!");
    }

    @Override
    public int executeUpdate() {
        bindParameters();
        return querySpecification.createModificationPlan(firstResult, maxResults).executeUpdate();
    }

    @Override
    public Query setHint(String hintName, Object value) {
        delegate.setHint(hintName, value);
        return this;
    }

    @Override
    public Map<String, Object> getHints() {
        return delegate.getHints();
    }

    @Override
    public Query setFlushMode(FlushModeType flushMode) {
        delegate.setFlushMode(flushMode);
        return this;
    }

    @Override
    public FlushModeType getFlushMode() {
        return delegate.getFlushMode();
    }

    @Override
    public Query setLockMode(LockModeType lockMode) {
        delegate.setLockMode(lockMode);
        return this;
    }

    @Override
    public LockModeType getLockMode() {
        return delegate.getLockMode();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        if (querySpecification.getParticipatingQueries().size() > 1) {
            throw new PersistenceException("Unsupported unwrap: " + cls.getName());
        }
        return delegate.unwrap(cls);
    }

    public Stream getResultStream() {
        throw new IllegalArgumentException("Can not call getResultList on a modification query!");
    }
}
