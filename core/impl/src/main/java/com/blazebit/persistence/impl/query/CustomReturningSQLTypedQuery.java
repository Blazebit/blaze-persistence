/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.query;

import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.impl.ParameterValueTransformer;
import com.blazebit.persistence.impl.ValuesParameterBinder;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;

import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Parameter;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.ParameterExpression;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CustomReturningSQLTypedQuery<T> extends AbstractCustomQuery<ReturningResult<T>> implements TypedQuery<ReturningResult<T>> {

    private final TypedQuery<?> delegate;

    public CustomReturningSQLTypedQuery(QuerySpecification<ReturningResult<T>> querySpecification, TypedQuery<?> delegate, Map<ParameterExpression<?>, String> criteriaNameMapping, Map<String, ParameterValueTransformer> transformers, Map<String, String> valuesParameters, Map<String, ValuesParameterBinder> valuesBinders) {
        super(querySpecification, criteriaNameMapping, transformers, valuesParameters, valuesBinders);
        this.delegate = delegate;
    }

    @Override
    public Query getDelegate() {
        return delegate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ReturningResult<T>> getResultList() {
        bindParameters();
        return querySpecification.createSelectPlan(firstResult, maxResults).getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ReturningResult<T> getSingleResult() {
        bindParameters();
        return querySpecification.createSelectPlan(firstResult, maxResults).getSingleResult();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ReturningResult<T> getSingleResultOrNull() {
        bindParameters();
        return querySpecification.createSelectPlan(firstResult, maxResults).getSingleResultOrNull();
    }

    @Override
    public TypedQuery<ReturningResult<T>> setCacheRetrieveMode(CacheRetrieveMode cacheRetrieveMode) {
        super.setCacheRetrieveMode(cacheRetrieveMode);
        return this;
    }

    @Override
    public TypedQuery<ReturningResult<T>> setCacheStoreMode(CacheStoreMode cacheStoreMode) {
        super.setCacheStoreMode(cacheStoreMode);
        return this;
    }

    @Override
    public TypedQuery<ReturningResult<T>> setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    @Override
    public int executeUpdate() {
        bindParameters();
        return querySpecification.createModificationPlan(firstResult, maxResults).executeUpdate();
    }

    @Override
    public TypedQuery<ReturningResult<T>> setHint(String hintName, Object value) {
        delegate.setHint(hintName, value);
        return this;
    }

    @Override
    public Map<String, Object> getHints() {
        return delegate.getHints();
    }

    @Override
    public TypedQuery<ReturningResult<T>> setFlushMode(FlushModeType flushMode) {
        delegate.setFlushMode(flushMode);
        return this;
    }

    @Override
    public FlushModeType getFlushMode() {
        return delegate.getFlushMode();
    }

    @Override
    public TypedQuery<ReturningResult<T>> setLockMode(LockModeType lockMode) {
        delegate.setLockMode(lockMode);
        return this;
    }

    @Override
    public LockModeType getLockMode() {
        return delegate.getLockMode();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        if (getParticipatingQueries().size() > 1) {
            throw new PersistenceException("Unsupported unwrap: " + cls.getName());
        }
        return delegate.unwrap(cls);
    }

    /* Covariant override */

    @Override
    public TypedQuery<ReturningResult<T>> setMaxResults(int maxResults) {
        super.setMaxResults(maxResults);
        return this;
    }

    @Override
    public TypedQuery<ReturningResult<T>> setFirstResult(int startPosition) {
        super.setFirstResult(startPosition);
        return this;
    }

    @Override
    public <X> TypedQuery<ReturningResult<T>> setParameter(Parameter<X> param, X value) {
        super.setParameter(param, value);
        return this;
    }

    @Override
    public TypedQuery<ReturningResult<T>> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        super.setParameter(param, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<ReturningResult<T>> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        super.setParameter(param, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<ReturningResult<T>> setParameter(String name, Object value) {
        super.setParameter(name, value);
        return this;
    }

    @Override
    public TypedQuery<ReturningResult<T>> setParameter(String name, Calendar value, TemporalType temporalType) {
        super.setParameter(name, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<ReturningResult<T>> setParameter(String name, Date value, TemporalType temporalType) {
        super.setParameter(name, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<ReturningResult<T>> setParameter(int position, Object value) {
        super.setParameter(position, value);
        return this;
    }

    @Override
    public TypedQuery<ReturningResult<T>> setParameter(int position, Calendar value, TemporalType temporalType) {
        super.setParameter(position, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<ReturningResult<T>> setParameter(int position, Date value, TemporalType temporalType) {
        super.setParameter(position, value, temporalType);
        return this;
    }

    public Stream<ReturningResult<T>> getResultStream() {
        bindParameters();
        return querySpecification.createSelectPlan(firstResult, maxResults).getResultStream();
    }

}
