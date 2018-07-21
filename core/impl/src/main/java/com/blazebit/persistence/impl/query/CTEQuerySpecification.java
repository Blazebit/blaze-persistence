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

package com.blazebit.persistence.impl.query;

import com.blazebit.persistence.impl.AbstractCommonQueryBuilder;
import com.blazebit.persistence.impl.plan.SelectQueryPlan;

import javax.persistence.Parameter;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CTEQuerySpecification extends CustomQuerySpecification<Object> {

    public CTEQuerySpecification(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> commonQueryBuilder, Query baseQuery, Set<Parameter<?>> parameters, Set<String> parameterListNames, String limit, String offset,
                                 List<String> keyRestrictedLeftJoinAliases, List<EntityFunctionNode> entityFunctionNodes) {
        super(commonQueryBuilder, baseQuery, parameters, parameterListNames, limit, offset, keyRestrictedLeftJoinAliases, entityFunctionNodes, false, Collections.EMPTY_LIST, false);
    }

    @Override
    public Query getBaseQuery() {
        return baseQuery;
    }

    @Override
    protected void initialize() {
        List<Query> participatingQueries = new ArrayList<Query>();
        for (Map.Entry<String, Collection<?>> entry : listParameters.entrySet()) {
            baseQuery.setParameter(entry.getKey(), entry.getValue());
        }

        String sqlQuery = extendedQuerySupport.getSql(em, baseQuery);
        StringBuilder sqlSb = applySqlTransformations(sqlQuery);
        // Need to inline LIMIT and OFFSET
        dbmsDialect.appendExtendedSql(sqlSb, statementType, false, true, null, limit, offset, null, null);
        participatingQueries.add(baseQuery);

        this.sql = sqlSb.toString();
        this.participatingQueries = participatingQueries;
        this.dirty = false;
    }

    @Override
    public SelectQueryPlan<Object> createSelectPlan(int firstResult, int maxResults) {
        throw new UnsupportedOperationException();
    }

}
