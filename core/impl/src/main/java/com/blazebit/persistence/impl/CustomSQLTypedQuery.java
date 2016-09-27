package com.blazebit.persistence.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.*;

import com.blazebit.persistence.CommonQueryBuilder;
import com.blazebit.persistence.spi.CteQueryWrapper;
import com.blazebit.persistence.spi.ExtendedQuerySupport;

public class CustomSQLTypedQuery<X> extends AbstractCustomQuery implements TypedQuery<X> {

	private final TypedQuery<X> delegate;

	public CustomSQLTypedQuery(List<Query> participatingQueries, TypedQuery<X> delegate, CommonQueryBuilder<?> cqb, ExtendedQuerySupport extendedQuerySupport, String sql, Map<String, String> valuesParameters, Map<String, ValuesParameterBinder> valuesBinders) {
		super(participatingQueries, cqb, extendedQuerySupport, sql, valuesParameters, valuesBinders);
		this.delegate = delegate;
	}

    @Override
    @SuppressWarnings("unchecked")
	public List<X> getResultList() {
		delegate.setFirstResult(firstResult);
		delegate.setMaxResults(maxResults);
		return (List<X>) extendedQuerySupport.getResultList(cqb, participatingQueries, delegate, sql);
	}

	@Override
    @SuppressWarnings("unchecked")
	public X getSingleResult() {
		delegate.setFirstResult(firstResult);
		delegate.setMaxResults(maxResults);
		return (X) extendedQuerySupport.getSingleResult(cqb, participatingQueries, delegate, sql);
	}

	@Override
	public int executeUpdate() {
		throw new IllegalArgumentException("Can not call executeUpdate on a select query!");
	}

	@Override
	public TypedQuery<X> setHint(String hintName, Object value) {
		// TODO: implement
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public Map<String, Object> getHints() {
		// TODO: implement
		throw new UnsupportedOperationException("Not yet implemented!");
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
		if (participatingQueries.size() > 1) {
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
}
