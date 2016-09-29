package com.blazebit.persistence.impl.plan;

import javax.persistence.Query;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DefaultModificationQueryPlan implements ModificationQueryPlan {

    private final Query query;
    private final int firstResult;
    private final int maxResults;

    public DefaultModificationQueryPlan(Query query, int firstResult, int maxResults) {
        this.query = query;
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    @Override
    public int executeUpdate() {
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        return query.executeUpdate();
    }

}
