package com.blazebit.persistence.impl;

import com.blazebit.persistence.CommonQueryBuilder;
import com.blazebit.persistence.ReturningObjectBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.ExtendedQuerySupport;

import javax.persistence.*;
import java.util.*;

public class CustomReturningSQLTypedQuery<T> extends AbstractCustomQuery implements TypedQuery<ReturningResult<T>> {

	private final TypedQuery<?> delegate;
	private final DbmsDialect dbmsDialect;
	private final ReturningObjectBuilder<T> objectBuilder;

	public CustomReturningSQLTypedQuery(List<Query> participatingQueries, TypedQuery<?> delegate, CommonQueryBuilder<?> cqb, ExtendedQuerySupport extendedQuerySupport, String sql, Map<String, String> valuesParameters, Map<String, ValuesParameterBinder> valuesBinders, DbmsDialect dbmsDialect, ReturningObjectBuilder<T> objectBuilder) {
		super(participatingQueries, cqb, extendedQuerySupport, sql, valuesParameters, valuesBinders);
		this.delegate = delegate;
		this.dbmsDialect = dbmsDialect;
		this.objectBuilder = objectBuilder;
	}

    @Override
    @SuppressWarnings("unchecked")
	public List<ReturningResult<T>> getResultList() {
		return Arrays.asList(getSingleResult());
	}

	@Override
    @SuppressWarnings("unchecked")
	public ReturningResult<T> getSingleResult() {
		delegate.setFirstResult(firstResult);
		delegate.setMaxResults(maxResults);
		// TODO: hibernate will return the object directly for single attribute case instead of an object array
		ReturningResult<Object[]> result = extendedQuerySupport.executeReturning(cqb, participatingQueries, delegate, sql);
		final List<Object[]> originalResultList = result.getResultList();
		final int updateCount = result.getUpdateCount();
		return new DefaultReturningResult<T>(originalResultList, updateCount, dbmsDialect, objectBuilder);
	}

	@Override
	public int executeUpdate() {
		ReturningResult<Object[]> result = extendedQuerySupport.executeReturning(cqb, participatingQueries, delegate, sql);
		return result.getUpdateCount();
	}

	@Override
	public TypedQuery<ReturningResult<T>> setHint(String hintName, Object value) {
		// TODO: implement
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public Map<String, Object> getHints() {
		// TODO: implement
		throw new UnsupportedOperationException("Not yet implemented!");
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
		if (participatingQueries.size() > 1) {
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

}
