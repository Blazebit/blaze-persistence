/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.plan;

import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.persistence.spi.ServiceProvider;

import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CustomSelectQueryPlan<T> implements SelectQueryPlan<T> {

    private final ExtendedQuerySupport extendedQuerySupport;
    private final ServiceProvider serviceProvider;
    private final Query baseQuery;
    private final Query delegate;
    private final List<Query> participatingQueries;
    private final String sql;
    private final int firstResult;
    private final int maxResults;
    private final boolean queryPlanCacheEnabled;

    public CustomSelectQueryPlan(ExtendedQuerySupport extendedQuerySupport, ServiceProvider serviceProvider, Query baseQuery, Query delegate, List<Query> participatingQueries, String sql, int firstResult, int maxResults, boolean queryPlanCacheEnabled) {
        this.extendedQuerySupport = extendedQuerySupport;
        this.serviceProvider = serviceProvider;
        this.baseQuery = baseQuery;
        this.delegate = delegate;
        this.participatingQueries = participatingQueries;
        this.sql = sql;
        this.firstResult = firstResult;
        this.maxResults = maxResults;
        this.queryPlanCacheEnabled = queryPlanCacheEnabled;
    }

    @Override
    public List<T> getResultList() {
        extendedQuerySupport.applyFirstResultMaxResults(baseQuery, firstResult, maxResults);
        return extendedQuerySupport.getResultList(serviceProvider, participatingQueries, delegate, sql, queryPlanCacheEnabled);
    }

    @Override
    public T getSingleResult() {
        extendedQuerySupport.applyFirstResultMaxResults(baseQuery, firstResult, maxResults);
        return (T) extendedQuerySupport.getSingleResult(serviceProvider, participatingQueries, delegate, sql, queryPlanCacheEnabled);
    }

    @Override
    public T getSingleResultOrNull() {
        try {
            return getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Stream<T> getResultStream() {
        extendedQuerySupport.applyFirstResultMaxResults(baseQuery, firstResult, maxResults);
        return (Stream<T>) extendedQuerySupport.getResultStream(serviceProvider, participatingQueries, delegate, sql, queryPlanCacheEnabled);
    }
}
