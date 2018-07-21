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
import com.blazebit.persistence.impl.plan.CustomModificationQueryPlan;
import com.blazebit.persistence.impl.plan.ModificationQueryPlan;
import com.blazebit.persistence.impl.plan.SelectQueryPlan;
import com.blazebit.persistence.spi.DbmsLimitHandler;
import com.blazebit.persistence.spi.DbmsModificationState;

import javax.persistence.Parameter;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ModificationQuerySpecification<T> extends CustomQuerySpecification<T> {

    protected final Query exampleQuery;
    protected final boolean isEmbedded;
    protected final String[] returningColumns;
    protected final Map<DbmsModificationState, String> includedModificationStates;
    protected final Map<String, String> returningAttributeBindingMap;

    protected Query query;

    public ModificationQuerySpecification(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> commonQueryBuilder, Query baseQuery, Query exampleQuery, Set<Parameter<?>> parameters, Set<String> parameterListNames, boolean recursive, List<CTENode> ctes, boolean shouldRenderCteNodes,
                                          boolean isEmbedded, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates, Map<String, String> returningAttributeBindingMap) {
        this(commonQueryBuilder, baseQuery, exampleQuery, parameters, parameterListNames, Collections.<String>emptyList(), Collections.<EntityFunctionNode>emptyList(), recursive, ctes, shouldRenderCteNodes, isEmbedded, returningColumns, includedModificationStates, returningAttributeBindingMap);
    }

    public ModificationQuerySpecification(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> commonQueryBuilder, Query baseQuery, Query exampleQuery, Set<Parameter<?>> parameters, Set<String> parameterListNames,
                                          List<String> keyRestrictedLeftJoinAliases, List<EntityFunctionNode> entityFunctionNodes, boolean recursive, List<CTENode> ctes, boolean shouldRenderCteNodes,
                                          boolean isEmbedded, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates, Map<String, String> returningAttributeBindingMap) {
        super(commonQueryBuilder, baseQuery, parameters, parameterListNames, null, null, keyRestrictedLeftJoinAliases, entityFunctionNodes, recursive, ctes, shouldRenderCteNodes);
        this.exampleQuery = exampleQuery;
        this.isEmbedded = isEmbedded;
        this.returningColumns = returningColumns;
        this.includedModificationStates = includedModificationStates;
        this.returningAttributeBindingMap = new HashMap<>(returningAttributeBindingMap);
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
        return new CustomModificationQueryPlan(extendedQuerySupport, serviceProvider, baseQuery, query, participatingQueries, finalSql);
    }

    @Override
    public SelectQueryPlan<T> createSelectPlan(int firstResult, int maxResults) {
        throw new UnsupportedOperationException();
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

    protected final void remapColumnExpressions(StringBuilder sqlSb, Map<String, String> columnExpressionRemappings) {
        remapColumnExpressions(sqlSb, columnExpressionRemappings, 0, sqlSb.length());
    }

    protected final void remapColumnExpressions(StringBuilder sqlSb, Map<String, String> columnExpressionRemappings, int startIndex, int endIndex) {
        // Replace usages of the owner entities id columns by the corresponding join table id columns
        for (Map.Entry<String, String> entry : columnExpressionRemappings.entrySet()) {
            String sourceExpression = entry.getKey();
            String targetExpression = entry.getValue();
            if (sourceExpression.equals(targetExpression)) {
                continue;
            }
            int index = startIndex;
            while ((index = sqlSb.indexOf(sourceExpression, index)) != -1 && index < endIndex) {
                sqlSb.replace(index, index + sourceExpression.length(), targetExpression);
                int delta = targetExpression.length() - sourceExpression.length();
                index += delta;
                endIndex += delta;
            }
        }
    }
}
