/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.query;

import com.blazebit.persistence.impl.ParameterValueTransformer;
import com.blazebit.persistence.impl.ValuesParameterBinder;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.ParameterExpression;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CustomSQLTypedQuery<X> extends AbstractCustomQuery<X> implements TypedQuery<X> {

    private final Query delegate;

    public CustomSQLTypedQuery(QuerySpecification querySpecification, Query delegate, Map<ParameterExpression<?>, String> criteriaNameMapping, Map<String, ParameterValueTransformer> transformers, Map<String, String> valuesParameters, Map<String, ValuesParameterBinder> valuesBinders) {
        super(querySpecification, criteriaNameMapping, transformers, valuesParameters, valuesBinders);
        this.delegate = delegate;
    }

    @Override
    public Query getDelegate() {
        return delegate;
    }

    @Override
    public List<X> getResultList() {
        bindParameters();
        return querySpecification.createSelectPlan(firstResult, maxResults).getResultList();
    }

    @Override
    public X getSingleResult() {
        bindParameters();
        return querySpecification.createSelectPlan(firstResult, maxResults).getSingleResult();
    }

    @Override
    public X getSingleResultOrNull() {
        bindParameters();
        return querySpecification.createSelectPlan(firstResult, maxResults).getSingleResultOrNull();
    }

    @Override
    public TypedQuery<X> setCacheRetrieveMode(CacheRetrieveMode cacheRetrieveMode) {
        ((jakarta.persistence.Query) getDelegate()).setCacheRetrieveMode(cacheRetrieveMode);
        return this;
    }

    @Override
    public TypedQuery<X> setCacheStoreMode(CacheStoreMode cacheStoreMode) {
        ((jakarta.persistence.Query) getDelegate()).setCacheStoreMode(cacheStoreMode);
        return this;
    }

    @Override
    public TypedQuery<X> setTimeout(Integer timeout) {
        ((jakarta.persistence.Query) getDelegate()).setTimeout(timeout);
        return this;
    }

    @Override
    public int executeUpdate() {
        throw new IllegalArgumentException("Can not call executeUpdate on a select query!");
    }

    @Override
    public TypedQuery<X> setHint(String hintName, Object value) {
        delegate.setHint(hintName, value);
        return this;
    }

    @Override
    public Map<String, Object> getHints() {
        return delegate.getHints();
    }

    @Override
    public TypedQuery<X> setFlushMode(FlushModeType flushMode) {
        delegate.setFlushMode(flushMode);
        return this;
    }

    @Override
    public FlushModeType getFlushMode() {
        return delegate.getFlushMode();
    }

    @Override
    public TypedQuery<X> setLockMode(LockModeType lockMode) {
        delegate.setLockMode(lockMode);
        return this;
    }

    @Override
    public LockModeType getLockMode() {
        return delegate.getLockMode();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        // TODO: This unperformant, I think we should introduce a hasParticiaptingQueries in QuerySpecification
        if (querySpecification.getParticipatingQueries().size() > 1) {
            throw new PersistenceException("Unsupported unwrap: " + cls.getName());
        }
        return delegate.unwrap(cls);
    }

    /* Covariant override */

    @Override
    public TypedQuery<X> setMaxResults(int maxResults) {
        super.setMaxResults(maxResults);
        return this;
    }

    @Override
    public TypedQuery<X> setFirstResult(int startPosition) {
        super.setFirstResult(startPosition);
        return this;
    }

    @Override
    public <T> TypedQuery<X> setParameter(Parameter<T> param, T value) {
        super.setParameter(param, value);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        super.setParameter(param, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        super.setParameter(param, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(String name, Object value) {
        super.setParameter(name, value);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(String name, Calendar value, TemporalType temporalType) {
        super.setParameter(name, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(String name, Date value, TemporalType temporalType) {
        super.setParameter(name, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(int position, Object value) {
        super.setParameter(position, value);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(int position, Calendar value, TemporalType temporalType) {
        super.setParameter(position, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(int position, Date value, TemporalType temporalType) {
        super.setParameter(position, value, temporalType);
        return this;
    }

    public Stream<X> getResultStream() {
        bindParameters();
        return querySpecification.createSelectPlan(firstResult, maxResults).getResultStream();
    }

}
