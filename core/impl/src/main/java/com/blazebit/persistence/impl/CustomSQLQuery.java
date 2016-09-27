package com.blazebit.persistence.impl;

import com.blazebit.persistence.CommonQueryBuilder;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.DbmsLimitHandler;
import com.blazebit.persistence.spi.ExtendedQuerySupport;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

public class CustomSQLQuery extends AbstractCustomQuery {

	private final Query delegate;
	private final Map<String, String> addedCtes;

	public CustomSQLQuery(List<Query> participatingQueries, Query delegate, CommonQueryBuilder<?> cqb, ExtendedQuerySupport extendedQuerySupport, String sql, Map<String, String> valuesParameters, Map<String, ValuesParameterBinder> valuesBinders, Map<String, String> addedCtes) {
		super(participatingQueries, cqb, extendedQuerySupport, sql, valuesParameters, valuesBinders);
		this.delegate = delegate;
		this.addedCtes = addedCtes;
	}

    public Map<String, String> getAddedCtes() {
        return addedCtes;
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
		final String finalSql;
		if (getFirstResult() > 0 || getMaxResults() != Integer.MAX_VALUE) {
			DbmsLimitHandler limitHandler = cqb.getService(DbmsDialect.class).createLimitHandler();

			Integer firstResult = null;
			Integer maxResults = null;

			if (getFirstResult() > 0) {
				firstResult = getFirstResult();
			}
			if (getMaxResults() != Integer.MAX_VALUE) {
				maxResults = getMaxResults();
			}

			delegate.setFirstResult(getFirstResult());
			delegate.setMaxResults(getMaxResults());
			finalSql = limitHandler.applySqlInlined(sql, false, maxResults, firstResult);
		} else {
			finalSql = sql;
		}
        return extendedQuerySupport.executeUpdate(cqb, participatingQueries, delegate, finalSql);
	}

	@Override
	public Query setHint(String hintName, Object value) {
		// TODO: implement
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public Map<String, Object> getHints() {
		// TODO: implement
		throw new UnsupportedOperationException("Not yet implemented!");
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
		if (participatingQueries.size() > 1) {
			throw new PersistenceException("Unsupported unwrap: " + cls.getName());
		}
		return delegate.unwrap(cls);
	}
}
