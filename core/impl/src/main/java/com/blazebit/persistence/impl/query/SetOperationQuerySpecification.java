/*
 * Copyright 2014 - 2021 Blazebit.
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
import com.blazebit.persistence.impl.plan.CustomSelectQueryPlan;
import com.blazebit.persistence.impl.plan.SelectQueryPlan;
import com.blazebit.persistence.spi.OrderByElement;
import com.blazebit.persistence.spi.SetOperationType;

import javax.persistence.Parameter;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SetOperationQuerySpecification<T> extends CustomQuerySpecification<T> {

    private final Query leftMostQuery;
    private final List<Query> setOperands;
    private final SetOperationType operator;
    private final List<? extends OrderByElement> orderByElements;
    private final boolean nested;

    public SetOperationQuerySpecification(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> commonQueryBuilder, Query leftMostQuery, Query baseQuery, List<Query> setOperands, SetOperationType operator,
                                          List<? extends OrderByElement> orderByElements, boolean nested, Collection<? extends Parameter<?>> parameters, Set<String> parameterListNames, String limit, String offset,
                                          List<String> keyRestrictedLeftJoinAliases, List<EntityFunctionNode> entityFunctionNodes, boolean recursive, List<CTENode> ctes, boolean shouldRenderCteNodes,
                                          boolean queryPlanCacheEnabled) {
        super(commonQueryBuilder, baseQuery, parameters, parameterListNames, limit, offset, keyRestrictedLeftJoinAliases, entityFunctionNodes, recursive, ctes, shouldRenderCteNodes, queryPlanCacheEnabled, null);
        this.leftMostQuery = leftMostQuery;
        this.setOperands = setOperands;
        this.operator = operator;
        this.orderByElements = orderByElements;
        this.nested = nested;
    }

    @Override
    public Query getBaseQuery() {
        return baseQuery;
    }

    @Override
    protected void initialize() {
        String sqlQuery;
        List<Query> participatingQueries = new ArrayList<Query>();
        List<Query> cteQueries = new ArrayList<Query>();

        bindListParameters(baseQuery);
        Query q = leftMostQuery;
        if (q instanceof TypedQueryWrapper<?>) {
            q = ((TypedQueryWrapper<?>) q).getDelegate();
        }
        if (q instanceof AbstractCustomQuery<?>) {
            AbstractCustomQuery<?> customQuery = (AbstractCustomQuery<?>) q;
            bindListParameters(q);
            List<Query> customQueryParticipants = customQuery.getParticipatingQueries();
            participatingQueries.addAll(customQueryParticipants);
            sqlQuery = customQuery.getSql();
        } else {
            participatingQueries.add(baseQuery);
            sqlQuery = extendedQuerySupport.getSql(em, baseQuery);
        }

        int size = sqlQuery.length() + 10;
        List<String> setOperands = new ArrayList<String>();
        setOperands.add(sqlQuery);

        for (Query query : this.setOperands) {
            String setOperandSql;

            bindListParameters(query);
            q = query;
            if (q instanceof TypedQueryWrapper<?>) {
                q = ((TypedQueryWrapper<?>) q).getDelegate();
            }
            if (q instanceof AbstractCustomQuery<?>) {
                AbstractCustomQuery<?> customQuery = (AbstractCustomQuery<?>) q;
                List<Query> customQueryParticipants = customQuery.getParticipatingQueries();
                participatingQueries.addAll(customQueryParticipants);

                setOperandSql = customQuery.getSql();
            } else {
                setOperandSql = extendedQuerySupport.getSql(em, q);
                participatingQueries.add(q);
            }

            setOperands.add(setOperandSql);
            size += setOperandSql.length() + 30;
        }

        StringBuilder sqlSb = new StringBuilder(size);

        dbmsDialect.appendSet(sqlSb, operator, nested, setOperands, orderByElements, limit, offset);
        StringBuilder withClause = applyCtes(sqlSb, baseQuery, cteQueries);
        // No limit/offset here since that has been taken care of before
        Map<String, String> addedCtes = dbmsDialect.appendExtendedSql(sqlSb, statementType, false, false, withClause, null, null, null, null, null);
        cteQueries.addAll(participatingQueries);
        participatingQueries = cteQueries;

        this.sql = sqlSb.toString();
        this.participatingQueries = participatingQueries;
        this.addedCtes = addedCtes;
        this.dirty = false;
    }

    @Override
    public SelectQueryPlan<T> createSelectPlan(int firstResult, int maxResults) {
        final String sql = getSql();
        return new CustomSelectQueryPlan<>(extendedQuerySupport, serviceProvider, baseQuery, baseQuery, participatingQueries, sql, firstResult, maxResults, queryPlanCacheEnabled);
    }

    private void bindListParameters(Query q) {
        for (Parameter<?> parameter : q.getParameters()) {
            Collection<?> value = listParameters.get(parameter.getName());
            if (value != null) {
                q.setParameter((Parameter) parameter, value);
            }
        }
    }
}
