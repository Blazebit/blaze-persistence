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

import com.blazebit.persistence.ReturningObjectBuilder;
import com.blazebit.persistence.impl.AbstractCommonQueryBuilder;
import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.DbmsModificationState;

import javax.persistence.Parameter;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CollectionInsertModificationQuerySpecification<T> extends ModificationQuerySpecification<T> {

    private final Query insertExampleQuery;
    private final String insertSql;
    private final int cutoffColumns;
    private final Collection<Query> foreignKeyParticipatingQueries;

    public CollectionInsertModificationQuerySpecification(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> commonQueryBuilder, Query baseQuery, Query exampleQuery, Collection<? extends Parameter<?>> parameters, Set<String> parameterListNames,
                                                          List<String> keyRestrictedLeftJoinAliases, List<EntityFunctionNode> entityFunctionNodes, boolean recursive, List<CTENode> ctes, boolean shouldRenderCteNodes,
                                                          boolean isEmbedded, String[] returningColumns, ReturningObjectBuilder<T> objectBuilder, Map<DbmsModificationState, String> includedModificationStates, Map<String, String> returningAttributeBindingMap,
                                                          Query insertExampleQuery, String insertSql, int cutoffColumns, Collection<Query> foreignKeyParticipatingQueries, boolean queryPlanCacheEnabled) {
        super(commonQueryBuilder, baseQuery, exampleQuery, parameters, parameterListNames, keyRestrictedLeftJoinAliases, entityFunctionNodes, recursive, ctes, shouldRenderCteNodes, isEmbedded, returningColumns, objectBuilder, includedModificationStates, returningAttributeBindingMap, queryPlanCacheEnabled);
        this.insertExampleQuery = insertExampleQuery;
        this.insertSql = insertSql;
        this.cutoffColumns = cutoffColumns;
        this.foreignKeyParticipatingQueries = foreignKeyParticipatingQueries;
    }

    @Override
    protected void initialize() {
        List<Query> participatingQueries = new ArrayList<>();

        for (Map.Entry<String, Collection<?>> entry : listParameters.entrySet()) {
            baseQuery.setParameter(entry.getKey(), entry.getValue());
        }

        String sql = extendedQuerySupport.getSql(em, baseQuery);
        StringBuilder sqlSb = applySqlTransformations(sql);
        if (cutoffColumns > 0) {
            final List<String> tableAliasesToRemove = new ArrayList<>();
            // Kind of a hack to reuse existing code to be able to cutoff columns at the end of the select list
            String[] selectItemPositions = SqlUtils.getSelectItems(sqlSb, 0, new SqlUtils.SelectItemExtractor() {
                @Override
                public String extract(StringBuilder sb, int index, int currentPosition) {
                    int dotIndex = sb.indexOf(".");
                    if (dotIndex == -1) {
                        tableAliasesToRemove.add("");
                    } else {
                        tableAliasesToRemove.add(sb.substring(0, dotIndex).trim());
                    }
                    return Integer.toString(currentPosition);
                }
            });
            int removeStart = Integer.parseInt(selectItemPositions[selectItemPositions.length - (cutoffColumns + 1)]);
            int removeEnd = Integer.parseInt(selectItemPositions[selectItemPositions.length - 1]);
            sqlSb.replace(removeStart, removeEnd, "");

            int endIndex = SqlUtils.indexOfWhere(sqlSb);
            if (endIndex == -1) {
                endIndex = SqlUtils.indexOfGroupBy(sqlSb, 0);
            }
            if (endIndex == -1) {
                endIndex = SqlUtils.indexOfHaving(sqlSb, 0);
            }
            if (endIndex == -1) {
                endIndex = SqlUtils.indexOfOrderBy(sqlSb, 0);
            }
            if (endIndex == -1) {
                endIndex = sqlSb.length();
            }
            // For every table alias we found in the select items that we removed due to the cutoff, we delete the joins
            Set<String> processedAliases = new HashSet<>();
            for (int i = tableAliasesToRemove.size() - cutoffColumns; i < tableAliasesToRemove.size(); i++) {
                String tableAlias = tableAliasesToRemove.get(i);
                if (!processedAliases.add(tableAlias)) {
                    continue;
                }
                String aliasOnPart = " " + tableAlias + " on ";
                int aliasIndex = sqlSb.indexOf(aliasOnPart, removeStart);
                if (aliasIndex > -1 && aliasIndex < endIndex) {
                    // First, let's find the end of the on clause
                    int onClauseStart = aliasIndex + aliasOnPart.length();
                    int onClauseEnd = SqlUtils.findEndOfOnClause(sqlSb, onClauseStart, endIndex);
                    int joinStartIndex = SqlUtils.findJoinStartIndex(sqlSb, aliasIndex);
                    sqlSb.replace(joinStartIndex, onClauseEnd, "");
                    endIndex -= onClauseEnd - joinStartIndex;
                }
            }
        }
        sqlSb.insert(0, insertSql);
        sqlSb.insert(insertSql.length(), ' ');

        String dmlAffectedTable = insertSql.substring("insert into ".length(), insertSql.indexOf('('));

        StringBuilder withClause = applyCtes(sqlSb, baseQuery, participatingQueries);
        // NOTE: CTEs will only be added, if this is a subquery
        Map<String, String> addedCtes = applyExtendedSql(sqlSb, false, isEmbedded, withClause, dmlAffectedTable, returningColumns, includedModificationStates);
        participatingQueries.add(baseQuery);
        participatingQueries.add(exampleQuery);
        participatingQueries.add(insertExampleQuery);
        participatingQueries.addAll(foreignKeyParticipatingQueries);

        // Some dbms like DB2 will need to wrap modification queries in select queries when using CTEs
        boolean hasCtes = withClause != null && withClause.length() != 0 || addedCtes != null && !addedCtes.isEmpty();
        if (hasCtes && returningAttributeBindingMap.isEmpty() && !dbmsDialect.usesExecuteUpdateWhenWithClauseInModificationQuery()) {
            query = exampleQuery;
        } else {
            query = insertExampleQuery;
        }

        this.sql = sqlSb.toString();
        this.participatingQueries = participatingQueries;
        this.addedCtes = addedCtes;
        this.dirty = false;
    }
}
