package com.blazebit.persistence.impl.plan;

import com.blazebit.persistence.spi.DbmsStatementType;

import javax.persistence.Query;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DefaultModificationQueryPlan implements ModificationQueryPlan {

    private final DbmsStatementType statementType;
    private final Query query;
    private final int firstResult;
    private final int maxResults;

    public DefaultModificationQueryPlan(DbmsStatementType statementType, Query query, int firstResult, int maxResults) {
        this.statementType = statementType;
        this.query = query;
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    @Override
    public int executeUpdate() {
        // Don't set the values for UPDATE or DELETE statements, otherwise Datanucleus will pass through the values to the JDBC statement
        if (statementType == DbmsStatementType.INSERT) {
            query.setFirstResult(firstResult);
            query.setMaxResults(maxResults);
        }
        return query.executeUpdate();
    }

}
