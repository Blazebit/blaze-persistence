/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.plan;

import javax.persistence.Query;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DefaultSelectQueryPlan<T> implements SelectQueryPlan<T> {

    private final Query query;
    private final int firstResult;
    private final int maxResults;

    public DefaultSelectQueryPlan(Query query, int firstResult, int maxResults) {
        this.query = query;
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    @Override
    public List<T> getResultList() {
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        return query.getResultList();
    }

    @Override
    public T getSingleResult() {
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        return (T) query.getSingleResult();
    }

    public Stream<T> getResultStream() {
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        return query.getResultStream();
    }

}
