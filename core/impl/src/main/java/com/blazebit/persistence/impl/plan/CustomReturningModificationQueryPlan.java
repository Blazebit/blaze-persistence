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

import com.blazebit.persistence.ReturningObjectBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.impl.DefaultReturningResult;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.persistence.spi.ServiceProvider;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CustomReturningModificationQueryPlan<T> implements ModificationQueryPlan, SelectQueryPlan<ReturningResult<T>> {

    private final ExtendedQuerySupport extendedQuerySupport;
    private final ServiceProvider serviceProvider;
    private final DbmsDialect dbmsDialect;
    private final Query modificationBaseQuery;
    private final Query delegate;
    private final ReturningObjectBuilder<T> objectBuilder;
    private final List<Query> participatingQueries;
    private final String sql;
    private final int firstResult;
    private final int maxResults;
    private final boolean requiresWrapping;

    public CustomReturningModificationQueryPlan(ExtendedQuerySupport extendedQuerySupport, ServiceProvider serviceProvider, Query modificationBaseQuery, Query delegate, ReturningObjectBuilder<T> objectBuilder, List<Query> participatingQueries, String sql, int firstResult, int maxResults, boolean requiresWrapping) {
        this.extendedQuerySupport = extendedQuerySupport;
        this.serviceProvider = serviceProvider;
        this.dbmsDialect = serviceProvider.getService(DbmsDialect.class);
        this.modificationBaseQuery = modificationBaseQuery;
        this.delegate = delegate;
        this.objectBuilder = objectBuilder;
        this.participatingQueries = participatingQueries;
        this.sql = sql;
        this.firstResult = firstResult;
        this.maxResults = maxResults;
        this.requiresWrapping = requiresWrapping;
    }

    @Override
    public int executeUpdate() {
        Query baseQuery = participatingQueries.get(0);
        baseQuery.setFirstResult(firstResult);
        baseQuery.setMaxResults(maxResults);
        ReturningResult<Object[]> result = extendedQuerySupport.executeReturning(serviceProvider, participatingQueries, modificationBaseQuery, delegate, sql);
        return result.getUpdateCount();
    }

    @Override
    public List<ReturningResult<T>> getResultList() {
        return Arrays.asList(getSingleResult());
    }

    @Override
    public ReturningResult<T> getSingleResult() {
        Query baseQuery = participatingQueries.get(0);
        baseQuery.setFirstResult(firstResult);
        baseQuery.setMaxResults(maxResults);

        ReturningResult<Object[]> result = extendedQuerySupport.executeReturning(serviceProvider, participatingQueries, modificationBaseQuery, delegate, sql);
        List<Object[]> resultList = result.getResultList();
        final int updateCount = result.getUpdateCount();
        if (requiresWrapping) {
            // NOTE: Hibernate will return the object directly for single attribute case instead of an object array
            int size = resultList.size();
            List<Object[]> newResultList = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                newResultList.add(new Object[]{ resultList.get(i) });
            }
            resultList = newResultList;
        }
        return new DefaultReturningResult<T>(resultList, updateCount, dbmsDialect, objectBuilder);
    }
}
