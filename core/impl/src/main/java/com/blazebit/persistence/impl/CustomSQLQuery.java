package com.blazebit.persistence.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import com.blazebit.persistence.spi.CteQueryWrapper;
import com.blazebit.persistence.spi.ExtendedQuerySupport;

public class CustomSQLQuery implements Query, CteQueryWrapper {

    private final List<Query> participatingQueries;
	private final Query delegate;
	private final EntityManager em;
	private final ExtendedQuerySupport extendedQuerySupport;
	private final String sql;
	
	public CustomSQLQuery(List<Query> participatingQueries, Query delegate, EntityManager em, ExtendedQuerySupport extendedQuerySupport, String sql) {
	    this.participatingQueries = participatingQueries;
		this.delegate = delegate;
		this.em = em;
		this.extendedQuerySupport = extendedQuerySupport;
		this.sql = sql;
	}

    public String getSql() {
        return sql;
    }
    
    @Override
    public List<Query> getParticipatingQueries() {
        return participatingQueries;
    }

	private Query wrapOrReturn(Query q) {
		if (q == delegate) {
			return this;
		}
		
		return new CustomSQLQuery(participatingQueries, q, em, extendedQuerySupport, sql);
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
	public int executeUpdate() {
		return extendedQuerySupport.executeUpdate(em, participatingQueries, delegate, sql);
	}

	@Override
	public Query setMaxResults(int maxResult) {
		return wrapOrReturn(delegate.setMaxResults(maxResult));
	}

	@Override
	public int getMaxResults() {
		return delegate.getMaxResults();
	}

	@Override
	public Query setFirstResult(int startPosition) {
		return wrapOrReturn(delegate.setFirstResult(startPosition));
	}

	@Override
	public int getFirstResult() {
		return delegate.getFirstResult();
	}

	@Override
	public Query setHint(String hintName, Object value) {
		return wrapOrReturn(delegate.setHint(hintName, value));
	}

	@Override
	public Map<String, Object> getHints() {
		return delegate.getHints();
	}

	@Override
	public <T> Query setParameter(Parameter<T> param, T value) {
		return wrapOrReturn(delegate.setParameter(param, value));
	}

	@Override
	public Query setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
		return wrapOrReturn(delegate.setParameter(param, value, temporalType));
	}

	@Override
	public Query setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
		return wrapOrReturn(delegate.setParameter(param, value, temporalType));
	}

	@Override
	public Query setParameter(String name, Object value) {
		return wrapOrReturn(delegate.setParameter(name, value));
	}

	@Override
	public Query setParameter(String name, Calendar value, TemporalType temporalType) {
		return wrapOrReturn(delegate.setParameter(name, value, temporalType));
	}

	@Override
	public Query setParameter(String name, Date value, TemporalType temporalType) {
		return wrapOrReturn(delegate.setParameter(name, value, temporalType));
	}

	@Override
	public Query setParameter(int position, Object value) {
		return wrapOrReturn(delegate.setParameter(position, value));
	}

	@Override
	public Query setParameter(int position, Calendar value, TemporalType temporalType) {
		return wrapOrReturn(delegate.setParameter(position, value, temporalType));
	}

	@Override
	public Query setParameter(int position, Date value, TemporalType temporalType) {
		return wrapOrReturn(delegate.setParameter(position, value, temporalType));
	}

	@Override
	public Set<Parameter<?>> getParameters() {
		return delegate.getParameters();
	}

	@Override
	public Parameter<?> getParameter(String name) {
		return delegate.getParameter(name);
	}

	@Override
	public <T> Parameter<T> getParameter(String name, Class<T> type) {
		return delegate.getParameter(name, type);
	}

	@Override
	public Parameter<?> getParameter(int position) {
		return delegate.getParameter(position);
	}

	@Override
	public <T> Parameter<T> getParameter(int position, Class<T> type) {
		return delegate.getParameter(position, type);
	}

	@Override
	public boolean isBound(Parameter<?> param) {
		return delegate.isBound(param);
	}

	@Override
	public <T> T getParameterValue(Parameter<T> param) {
		return delegate.getParameterValue(param);
	}

	@Override
	public Object getParameterValue(String name) {
		return delegate.getParameterValue(name);
	}

	@Override
	public Object getParameterValue(int position) {
		return delegate.getParameterValue(position);
	}

	@Override
	public Query setFlushMode(FlushModeType flushMode) {
		return wrapOrReturn(delegate.setFlushMode(flushMode));
	}

	@Override
	public FlushModeType getFlushMode() {
		return delegate.getFlushMode();
	}

	@Override
	public Query setLockMode(LockModeType lockMode) {
		return wrapOrReturn(delegate.setLockMode(lockMode));
	}

	@Override
	public LockModeType getLockMode() {
		return delegate.getLockMode();
	}

	@Override
	public <T> T unwrap(Class<T> cls) {
		return delegate.unwrap(cls);
	}
}
