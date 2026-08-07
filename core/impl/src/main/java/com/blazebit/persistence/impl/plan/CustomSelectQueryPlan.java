/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.plan;

import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.persistence.spi.ServiceProvider;

import javax.persistence.NoResultException;
import javax.persistence.Query;
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

    /**
     * Applies limit/offset via the Blaze {@link com.blazebit.persistence.spi.DbmsLimitHandler} so that a
     * leading {@code WITH} clause stays outside of Oracle's {@code ROWNUM} pagination wrapper.
     * Relying on the JPA provider's limit handler for custom/CTE SQL wraps the whole statement
     * (including the CTE), which produces invalid Oracle SQL and parameter index errors (#2091).
     */
    private String applyLimitOffset(String sql) {
        if (firstResult == 0 && maxResults == Integer.MAX_VALUE) {
            extendedQuerySupport.applyFirstResultMaxResults(baseQuery, firstResult, maxResults);
            return sql;
        }
        Integer limit = maxResults == Integer.MAX_VALUE ? null : maxResults;
        Integer offset = firstResult == 0 ? null : firstResult;
        String limitedSql = serviceProvider.getService(DbmsDialect.class)
                .createLimitHandler()
                .applySqlInlined(sql, false, limit, offset);
        // Clear provider-side first/max so the limit is not applied a second time on the overridden SQL
        extendedQuerySupport.applyFirstResultMaxResults(baseQuery, 0, Integer.MAX_VALUE);
        return limitedSql;
    }

    @Override
    public List<T> getResultList() {
        String finalSql = applyLimitOffset(sql);
        return extendedQuerySupport.getResultList(serviceProvider, participatingQueries, delegate, finalSql, queryPlanCacheEnabled);
    }

    @Override
    public T getSingleResult() {
        String finalSql = applyLimitOffset(sql);
        return (T) extendedQuerySupport.getSingleResult(serviceProvider, participatingQueries, delegate, finalSql, queryPlanCacheEnabled);
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
        String finalSql = applyLimitOffset(sql);
        return (Stream<T>) extendedQuerySupport.getResultStream(serviceProvider, participatingQueries, delegate, finalSql, queryPlanCacheEnabled);
    }
}
