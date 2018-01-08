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

import javax.persistence.Query;
import java.util.List;

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
}
