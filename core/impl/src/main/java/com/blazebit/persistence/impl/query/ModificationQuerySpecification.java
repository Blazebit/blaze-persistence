/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.impl.query;

import com.blazebit.persistence.impl.AbstractCommonQueryBuilder;
import com.blazebit.persistence.impl.plan.CustomModificationQueryPlan;
import com.blazebit.persistence.impl.plan.ModificationQueryPlan;
import com.blazebit.persistence.impl.plan.SelectQueryPlan;
import com.blazebit.persistence.spi.DbmsLimitHandler;
import com.blazebit.persistence.spi.DbmsModificationState;

import javax.persistence.Query;
import java.util.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ModificationQuerySpecification extends CustomQuerySpecification {

    private final Query exampleQuery;
    private final boolean isEmbedded;
    private final String[] returningColumns;
    private final Map<DbmsModificationState, String> includedModificationStates;
    private final Map<String, String> returningAttributeBindingMap;

    private Query query;

    public ModificationQuerySpecification(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> commonQueryBuilder, Query baseQuery, Query exampleQuery, Set<String> parameterListNames, boolean recursive, List<CTENode> ctes, boolean shouldRenderCteNodes,
        boolean isEmbedded, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates, Map<String, String> returningAttributeBindingMap) {
        super(commonQueryBuilder, baseQuery, parameterListNames, null, null, Collections.EMPTY_LIST, Collections.EMPTY_LIST, recursive, ctes, shouldRenderCteNodes);
        this.exampleQuery = exampleQuery;
        this.isEmbedded = isEmbedded;
        this.returningColumns = returningColumns;
        this.includedModificationStates = includedModificationStates;
        this.returningAttributeBindingMap = new HashMap<String, String>(returningAttributeBindingMap);
    }

    @Override
    public ModificationQueryPlan createModificationPlan(int firstResult, int maxResults) {
        final String sql = getSql();
        final String finalSql;
        if (firstResult != 0 || maxResults != Integer.MAX_VALUE) {
            DbmsLimitHandler limitHandler = dbmsDialect.createLimitHandler();
            finalSql = limitHandler.applySqlInlined(sql, false, maxResults, firstResult);
        } else {
            finalSql = sql;
        }
        return new CustomModificationQueryPlan(extendedQuerySupport, serviceProvider, query, participatingQueries, finalSql);
    }

    @Override
    public SelectQueryPlan createSelectPlan(int firstResult, int maxResults) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query getBaseQuery() {
        return baseQuery;
    }

    @Override
    protected void initialize() {
        List<Query> participatingQueries = new ArrayList<Query>();

        StringBuilder sqlSb = new StringBuilder(extendedQuerySupport.getSql(em, baseQuery));
        StringBuilder withClause = applyCtes(sqlSb, baseQuery, participatingQueries);
        // NOTE: CTEs will only be added, if this is a subquery
        Map<String, String> addedCtes = applyExtendedSql(sqlSb, false, isEmbedded, withClause, returningColumns, includedModificationStates);
        participatingQueries.add(baseQuery);

        // Some dbms like DB2 will need to wrap modification queries in select queries when using CTEs
        boolean hasCtes = withClause != null && withClause.length() != 0 || addedCtes != null && !addedCtes.isEmpty();
        if (hasCtes && returningAttributeBindingMap.isEmpty() && !dbmsDialect.usesExecuteUpdateWhenWithClauseInModificationQuery()) {
            query = exampleQuery;
        } else {
            query = baseQuery;
        }

        this.sql = sqlSb.toString();
        this.participatingQueries = participatingQueries;
        this.addedCtes = addedCtes;
        this.dirty = false;
    }
}
