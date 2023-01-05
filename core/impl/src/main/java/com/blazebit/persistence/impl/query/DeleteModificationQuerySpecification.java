/*
 * Copyright 2014 - 2023 Blazebit.
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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class DeleteModificationQuerySpecification<T> extends ModificationQuerySpecification<T> {

    private final String tableToDelete;
    private final String tableAlias;
    private final String[] idColumns;
    private final boolean innerJoinOnly;
    private final Query deleteExampleQuery;

    public DeleteModificationQuerySpecification(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> commonQueryBuilder, Query baseQuery, Query exampleQuery, Collection<? extends Parameter<?>> parameters, Set<String> parameterListNames,
                                                List<String> keyRestrictedLeftJoinAliases, List<EntityFunctionNode> entityFunctionNodes, boolean recursive, List<CTENode> ctes, boolean shouldRenderCteNodes,
                                                boolean isEmbedded, String[] returningColumns, ReturningObjectBuilder<T> objectBuilder, Map<DbmsModificationState, String> includedModificationStates, Map<String, String> returningAttributeBindingMap, boolean queryPlanCacheEnabled, String tableToDelete, String tableAlias, String[] idColumns, boolean innerJoinOnly, Query deleteExampleQuery) {
        super(commonQueryBuilder, baseQuery, exampleQuery, parameters, parameterListNames, keyRestrictedLeftJoinAliases, entityFunctionNodes, recursive, ctes, shouldRenderCteNodes, isEmbedded, returningColumns, objectBuilder, includedModificationStates, returningAttributeBindingMap, queryPlanCacheEnabled);
        this.tableToDelete = tableToDelete;
        this.tableAlias = tableAlias;
        this.idColumns = idColumns;
        this.innerJoinOnly = innerJoinOnly;
        this.deleteExampleQuery = deleteExampleQuery;
    }

    @Override
    protected void initialize() {
        List<Query> participatingQueries = new ArrayList<>();

        for (Map.Entry<String, Collection<?>> entry : listParameters.entrySet()) {
            baseQuery.setParameter(entry.getKey(), entry.getValue());
        }

        String sql = applySqlTransformations(extendedQuerySupport.getSql(em, baseQuery)).toString();
        StringBuilder sb = new StringBuilder(sql.length());
        String tableToDelete = this.tableToDelete;
        if (tableToDelete == null) {
            // Plain delete statement without joins
            sb.append(sql);
            int fromIndex = SqlUtils.indexOfFrom(sql);
            tableToDelete = sql.substring(fromIndex + SqlUtils.FROM.length(), sql.indexOf(' ', fromIndex + SqlUtils.FROM.length() + 1));
        } else {
            int fromIndex = SqlUtils.indexOfFrom(sql);
            int tableStartIndex = fromIndex + SqlUtils.FROM.length();
            int tableEndIndex = sql.indexOf(" ", tableStartIndex);
            int tableAliasEndIndex = sql.indexOf(" ", tableEndIndex + 1);
            switch (dbmsDialect.getDeleteJoinStyle()) {
                case FROM:
                    sb.append("delete ").append(tableAlias);
                    sb.append(sql, fromIndex, sql.length());
                    break;
                case USING:
                    sb.append("delete from ");
                    if (innerJoinOnly) {
                        sb.append(tableToDelete).append(' ').append(tableAlias);
                        sb.append(" using ");
                        int onClauseIndex = SqlUtils.indexOfOn(sql, tableAliasEndIndex);
                        int onClauseEndIndex = SqlUtils.findEndOfOnClause(sql, onClauseIndex, SqlUtils.indexOfWhere(sql));
                        // The lower bound is the start of the table alias
                        int[] range = SqlUtils.rtrimBackwardsToFirstWhitespace(sql, onClauseIndex - 1);
                        // The new lower bound is the start of the table name
                        // TODO: this could be a problem when supporting subqueries..
                        range = SqlUtils.rtrimBackwardsToFirstWhitespace(sql, range[0] - 1);
                        int joinTableStartIndex = range[0];
                        int predicateStartIndex = onClauseIndex + SqlUtils.ON.length();
                        sb.append(sql, joinTableStartIndex, onClauseIndex);
                        sb.append(sql, onClauseEndIndex, sql.length());
                        sb.append(" and ").append(sql, predicateStartIndex, onClauseEndIndex);
                    } else {
                        sb.append(tableToDelete);
                        sb.append(" using ");
                        sb.append(sql, tableStartIndex, sql.length());

                        for (String idColumn : idColumns) {
                            sb.append(" and ").append(tableToDelete).append('.').append(idColumn).append(" = ").append(tableAlias).append('.').append(idColumn);
                        }
                    }

                    break;
                case NONE:
                case MERGE:
                    // This is only used for collection deletes and always uses an exists subquery
                    int whereIndex = SqlUtils.indexOfWhere(sql);
                    sb.append("delete ").append(tableToDelete);
                    if (whereIndex != -1) {
                        sb.append(sql, whereIndex, sql.length());
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported delete join style: " + dbmsDialect.getDeleteJoinStyle());
            }
        }

        StringBuilder withClause = applyCtes(sb, baseQuery, participatingQueries);
        // NOTE: CTEs will only be added, if this is a subquery
        Map<String, String> addedCtes = applyExtendedSql(sb, false, isEmbedded, withClause, tableToDelete, returningColumns, includedModificationStates);
        participatingQueries.add(baseQuery);
        participatingQueries.add(exampleQuery);
        participatingQueries.add(deleteExampleQuery);

        boolean hasCtes = withClause != null && withClause.length() != 0 || addedCtes != null && !addedCtes.isEmpty();
        if (hasCtes && returningAttributeBindingMap.isEmpty() && !dbmsDialect.usesExecuteUpdateWhenWithClauseInModificationQuery()) {
            query = exampleQuery;
        } else {
            query = deleteExampleQuery;
        }

        this.sql = sb.toString();
        this.participatingQueries = participatingQueries;
        this.addedCtes = addedCtes;
        this.dirty = false;
    }
}
