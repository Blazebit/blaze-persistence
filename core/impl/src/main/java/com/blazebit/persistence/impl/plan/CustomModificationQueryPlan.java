/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.plan;

import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.persistence.spi.ServiceProvider;

import jakarta.persistence.Query;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CustomModificationQueryPlan implements ModificationQueryPlan {

    private final ExtendedQuerySupport extendedQuerySupport;
    private final ServiceProvider serviceProvider;
    private final Query baseQuery;
    private final Query delegate;
    private final List<Query> participatingQueries;
    private final String sql;
    private final boolean queryPlanCacheEnabled;

    public CustomModificationQueryPlan(ExtendedQuerySupport extendedQuerySupport, ServiceProvider serviceProvider, Query baseQuery, Query delegate, List<Query> participatingQueries, String sql, boolean queryPlanCacheEnabled) {
        this.extendedQuerySupport = extendedQuerySupport;
        this.serviceProvider = serviceProvider;
        this.baseQuery = baseQuery;
        this.delegate = delegate;
        this.participatingQueries = participatingQueries;
        this.sql = sql;
        this.queryPlanCacheEnabled = queryPlanCacheEnabled;
    }

    @Override
    public int executeUpdate() {
        return extendedQuerySupport.executeUpdate(serviceProvider, participatingQueries, baseQuery, delegate, sql, queryPlanCacheEnabled);
    }

}
