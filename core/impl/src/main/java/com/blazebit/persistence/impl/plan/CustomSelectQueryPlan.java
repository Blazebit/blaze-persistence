package com.blazebit.persistence.impl.plan;

import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.persistence.spi.ServiceProvider;

import javax.persistence.Query;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CustomSelectQueryPlan<T> implements SelectQueryPlan<T> {

    private final ExtendedQuerySupport extendedQuerySupport;
    private final ServiceProvider serviceProvider;
    private final Query delegate;
    private final List<Query> participatingQueries;
    private final String sql;
    private final int firstResult;
    private final int maxResults;

    public CustomSelectQueryPlan(ExtendedQuerySupport extendedQuerySupport, ServiceProvider serviceProvider, Query delegate, List<Query> participatingQueries, String sql, int firstResult, int maxResults) {
        this.extendedQuerySupport = extendedQuerySupport;
        this.serviceProvider = serviceProvider;
        this.delegate = delegate;
        this.participatingQueries = participatingQueries;
        this.sql = sql;
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    @Override
    public List<T> getResultList() {
        delegate.setFirstResult(firstResult);
        delegate.setMaxResults(maxResults);
        return extendedQuerySupport.getResultList(serviceProvider, participatingQueries, delegate, sql);
    }

    @Override
    public T getSingleResult() {
        delegate.setFirstResult(firstResult);
        delegate.setMaxResults(maxResults);
        return (T) extendedQuerySupport.getSingleResult(serviceProvider, participatingQueries, delegate, sql);
    }
}
