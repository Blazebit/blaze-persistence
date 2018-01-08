/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
