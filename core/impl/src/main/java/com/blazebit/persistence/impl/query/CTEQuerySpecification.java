package com.blazebit.persistence.impl.query;

import com.blazebit.persistence.impl.AbstractCommonQueryBuilder;
import com.blazebit.persistence.impl.plan.SelectQueryPlan;

import javax.persistence.Query;
import java.util.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CTEQuerySpecification extends CustomQuerySpecification {

    public CTEQuerySpecification(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> commonQueryBuilder, Query baseQuery, Set<String> parameterListNames, String limit, String offset) {
        super(commonQueryBuilder, baseQuery, parameterListNames, limit, offset, Collections.EMPTY_LIST, Collections.EMPTY_LIST, false, Collections.EMPTY_LIST, false);
    }

    @Override
    public Query getBaseQuery() {
        return baseQuery;
    }

    @Override
    protected void initialize() {
        List<Query> participatingQueries = Arrays.asList(baseQuery);

        StringBuilder sqlSb = new StringBuilder(extendedQuerySupport.getSql(em, baseQuery));
        // Need to inline LIMIT and OFFSET
        dbmsDialect.appendExtendedSql(sqlSb, statementType, false, true, null, limit, offset, null, null);

        this.sql = sqlSb.toString();
        this.participatingQueries = participatingQueries;
        this.dirty = false;
    }

    @Override
    public SelectQueryPlan createSelectPlan(int firstResult, int maxResults) {
        throw new UnsupportedOperationException();
    }

}
