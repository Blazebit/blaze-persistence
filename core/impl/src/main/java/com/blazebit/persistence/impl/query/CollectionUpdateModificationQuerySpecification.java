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
import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.DbmsModificationState;

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
public class CollectionUpdateModificationQuerySpecification<T> extends ModificationQuerySpecification<T> {

    public static final String COLLECTION_BASE_QUERY_ALIAS = "_collection";

    private final Query updateExampleQuery;
    private final String updateSql;
    private final List<Query> setExpressionContainingUpdateQueries;
    private final Map<String, String> columnOnlyRemappings;
    private final Map<String, String> columnExpressionRemappings;

    public CollectionUpdateModificationQuerySpecification(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> commonQueryBuilder, Query baseQuery, Query exampleQuery, Set<Parameter<?>> parameters, Set<String> parameterListNames, boolean recursive, List<CTENode> ctes, boolean shouldRenderCteNodes, boolean isEmbedded, String[] returningColumns,
                                                          Map<DbmsModificationState, String> includedModificationStates, Map<String, String> returningAttributeBindingMap, Query updateExampleQuery, String updateSql, List<Query> setExpressionContainingUpdateQueries, Map<String, String> columnOnlyRemappings, Map<String, String> columnExpressionRemappings) {
        super(commonQueryBuilder, baseQuery, exampleQuery, parameters, parameterListNames, recursive, ctes, shouldRenderCteNodes, isEmbedded, returningColumns, includedModificationStates, returningAttributeBindingMap);
        this.updateExampleQuery = updateExampleQuery;
        this.updateSql = updateSql;
        this.setExpressionContainingUpdateQueries = setExpressionContainingUpdateQueries;
        this.columnOnlyRemappings = columnOnlyRemappings;
        this.columnExpressionRemappings = columnExpressionRemappings;
    }

    @Override
    protected void initialize() {
        List<Query> participatingQueries = new ArrayList<Query>();

        for (Map.Entry<String, Collection<?>> entry : listParameters.entrySet()) {
            baseQuery.setParameter(entry.getKey(), entry.getValue());
        }

        StringBuilder sqlSb = new StringBuilder(extendedQuerySupport.getSql(em, baseQuery));
        StringBuilder setClauseSqlSb = new StringBuilder(updateSql);

        // The queries are in sequence, each containing set clause entries for the source or target table
        for (Query updateQuery : setExpressionContainingUpdateQueries) {
            participatingQueries.add(updateQuery);
            String setExpressionSql = extendedQuerySupport.getSql(em, updateQuery);
            int assignIndex = SqlUtils.indexOfWhere(setExpressionSql) + " where ".length();
            // TODO: fix this for row values/embeddables which might have parenthesis around or use OR
            int columnOnlyRemappingStartIndex = setClauseSqlSb.length();
            int columnOnlyRemappingEndIndex = columnOnlyRemappingStartIndex + (setExpressionSql.indexOf('=', assignIndex) - assignIndex);
            setClauseSqlSb.append(setExpressionSql, assignIndex, setExpressionSql.length());
            setClauseSqlSb.append(',');
            remapColumnExpressions(setClauseSqlSb, columnOnlyRemappings, columnOnlyRemappingStartIndex, columnOnlyRemappingEndIndex);
            remapColumnExpressions(setClauseSqlSb, columnExpressionRemappings, columnOnlyRemappingEndIndex, setClauseSqlSb.length());
        }
        setClauseSqlSb.setLength(setClauseSqlSb.length() - 1);

        // Replace the "select ... from ..." part of the base query by the "update collectionTable" part
        int whereIndex = SqlUtils.indexOfWhere(sqlSb);
        if (whereIndex == -1) {
            sqlSb.setLength(0);
            sqlSb.append(setClauseSqlSb);
        } else {
            sqlSb.replace(0, whereIndex, setClauseSqlSb.toString());
        }

        remapColumnExpressions(sqlSb, columnExpressionRemappings);

        StringBuilder withClause = applyCtes(sqlSb, baseQuery, participatingQueries);
        // NOTE: CTEs will only be added, if this is a subquery
        Map<String, String> addedCtes = applyExtendedSql(sqlSb, false, isEmbedded, withClause, returningColumns, includedModificationStates);
        participatingQueries.add(baseQuery);

        // Some dbms like DB2 will need to wrap modification queries in select queries when using CTEs
        boolean hasCtes = withClause != null && withClause.length() != 0 || addedCtes != null && !addedCtes.isEmpty();
        if (hasCtes && returningAttributeBindingMap.isEmpty() && !dbmsDialect.usesExecuteUpdateWhenWithClauseInModificationQuery()) {
            query = exampleQuery;
        } else {
            query = updateExampleQuery;
        }

        this.sql = sqlSb.toString();
        this.participatingQueries = participatingQueries;
        this.addedCtes = addedCtes;
        this.dirty = false;
    }
}
