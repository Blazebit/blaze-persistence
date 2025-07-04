/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.query;

import com.blazebit.persistence.ReturningObjectBuilder;
import com.blazebit.persistence.impl.AbstractCommonQueryBuilder;
import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.UpdateJoinStyle;

import javax.persistence.Parameter;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class UpdateModificationQuerySpecification<T> extends ModificationQuerySpecification<T> {

    private final String tableToUpdate;
    private final String tableAlias;
    private final String[] idColumns;
    private final List<String> setColumns;
    private final Collection<Query> foreignKeyParticipatingQueries;
    private final Map<String, String> aliasMapping;
    private final Query updateExampleQuery;

    public UpdateModificationQuerySpecification(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> commonQueryBuilder, Query baseQuery, Query exampleQuery, Collection<? extends Parameter<?>> parameters, Set<String> parameterListNames,
                                                List<String> keyRestrictedLeftJoinAliases, List<EntityFunctionNode> entityFunctionNodes, boolean recursive, List<CTENode> ctes, boolean shouldRenderCteNodes,
                                                boolean isEmbedded, String[] returningColumns, ReturningObjectBuilder<T> objectBuilder, Map<DbmsModificationState, String> includedModificationStates, Map<String, String> returningAttributeBindingMap, boolean queryPlanCacheEnabled,
                                                String tableToUpdate, String tableAlias, String[] idColumns, List<String> setColumns, Collection<Query> foreignKeyParticipatingQueries, Map<String, String> aliasMapping, Query updateExampleQuery) {
        super(commonQueryBuilder, baseQuery, exampleQuery, parameters, parameterListNames, keyRestrictedLeftJoinAliases, entityFunctionNodes, recursive, ctes, shouldRenderCteNodes, isEmbedded, returningColumns, objectBuilder, includedModificationStates, returningAttributeBindingMap, queryPlanCacheEnabled);
        this.tableToUpdate = tableToUpdate;
        this.tableAlias = tableAlias;
        this.idColumns = idColumns;
        this.setColumns = setColumns;
        this.foreignKeyParticipatingQueries = foreignKeyParticipatingQueries;
        this.aliasMapping = aliasMapping;
        this.updateExampleQuery = updateExampleQuery;
    }

    @Override
    protected void initialize() {
        List<Query> participatingQueries = new ArrayList<>();

        for (Map.Entry<String, Collection<?>> entry : listParameters.entrySet()) {
            baseQuery.setParameter(entry.getKey(), entry.getValue());
        }

        String sql = applySqlTransformations(extendedQuerySupport.getSql(em, baseQuery)).toString();
        StringBuilder sb = new StringBuilder(sql.length());
        String tableToUpdate = this.tableToUpdate;
        if (SqlUtils.indexOfSelect(sql) == -1) {
            // Plain update statement without joins
            sb.append(sql);
            tableToUpdate = sql.substring(sql.indexOf(' ') + 1, sql.indexOf(' ', sql.indexOf(' ') + 1));
        } else {
            int fromIndex = SqlUtils.indexOfFrom(sql);
            List<String> setItems = Arrays.asList(SqlUtils.getSelectItemExpressions(sql, 0));
            switch (dbmsDialect.getUpdateJoinStyle()) {
                case FROM:
                case FROM_ALIAS:
                    sb.append("update ");
                    if (dbmsDialect.getUpdateJoinStyle() == UpdateJoinStyle.FROM_ALIAS) {
                        sb.append(tableAlias);
                    } else {
                        sb.append(tableToUpdate);
                    }
                    sb.append(" set ");
                    for (int i = 0; i < setItems.size(); i++) {
                        if (i != 0) {
                            sb.append(", ");
                        }
                        String item = setItems.get(i);
                        sb.append(setColumns.get(i)).append(" = ");
                        sb.append(item, item.indexOf('=') + 1, item.lastIndexOf(" then "));
                    }
                    sb.append(sql, fromIndex, sql.length());
                    if (dbmsDialect.getUpdateJoinStyle() == UpdateJoinStyle.FROM) {
                        sb.append(" and ").append(tableToUpdate).append('.').append(idColumns[0]).append(" = ").append(tableAlias).append(".").append(idColumns[0]);
                        for (int i = 1; i < idColumns.length; i++) {
                            String idColumn = idColumns[i];
                            sb.append(" and ").append(tableToUpdate).append('.').append(idColumn).append(" = ").append(tableAlias).append(".").append(idColumns[i]);
                        }
                    }
                    break;
                case REFERENCE:
                    sb.append("update ");
                    sb.append(tableToUpdate);

                    sb.append(", (select ");
                    for (Map.Entry<String, String> entry : aliasMapping.entrySet()) {
                        sb.append(entry.getKey()).append(" as ").append(entry.getValue(), entry.getValue().indexOf('.') + 1, entry.getValue().length()).append(", ");
                    }
                    for (int i = 0; i < setItems.size(); i++) {
                        String item = setItems.get(i);
                        sb.append("coalesce(nullif(").append(tableAlias).append( '.' ).append(setColumns.get(i));
                        sb.append(",").append(tableAlias).append('.').append(setColumns.get(i)).append("),");
                        sb.append(item, item.indexOf('=') + 1, item.lastIndexOf(" then "));
                        sb.append(") as c").append(aliasMapping.size() + i).append( ", " );
                    }
                    sb.setLength(sb.length() - 2);
                    sb.append(sql, fromIndex, sql.length());
                    sb.append(") tmp ");

                    sb.append("set ");
                    for (int i = 0; i < setItems.size(); i++) {
                        if (i != 0) {
                            sb.append(", ");
                        }
                        sb.append(setColumns.get(i)).append(" = ");
                        sb.append("tmp.c").append(aliasMapping.size() + i);
                    }

                    sb.append(" where ");
                    sb.append(tableToUpdate).append('.').append(idColumns[0]).append(" = tmp.c0");
                    for (int i = 1; i < idColumns.length; i++) {
                        String idColumn = idColumns[i];
                        sb.append(" and ").append(tableToUpdate).append('.').append(idColumn).append(" = tmp.c").append(i);
                    }

                    break;
                case MERGE:
                    sb.append("merge into ");
                    sb.append(tableToUpdate);

                    sb.append(" using (select ");
                    for (Map.Entry<String, String> entry : aliasMapping.entrySet()) {
                        sb.append(entry.getKey()).append(" as ").append(entry.getValue(), entry.getValue().indexOf('.') + 1, entry.getValue().length()).append(", ");
                    }
                    for (int i = 0; i < setItems.size(); i++) {
                        String item = setItems.get(i);
                        sb.append("coalesce(nullif(").append(tableAlias).append( '.' ).append(setColumns.get(i));
                        sb.append(",").append(tableAlias).append('.').append(setColumns.get(i)).append("),");
                        sb.append(item, item.indexOf('=') + 1, item.lastIndexOf(" then "));
                        sb.append(") as c").append(aliasMapping.size() + i).append( ", " );
                    }
                    sb.setLength(sb.length() - 2);
                    sb.append(sql, fromIndex, sql.length());
                    sb.append(") tmp on (");
                    sb.append(tableToUpdate).append('.').append(idColumns[0]).append(" = tmp.c0");
                    for (int i = 1; i < idColumns.length; i++) {
                        String idColumn = idColumns[i];
                        sb.append(" and ").append(tableToUpdate).append('.').append(idColumn).append(" = tmp.c").append(i);
                    }
                    sb.append(") when matched then update set ");
                    for (int i = 0; i < setItems.size(); i++) {
                        if (i != 0) {
                            sb.append(", ");
                        }
                        sb.append(setColumns.get(i)).append(" = ");
                        sb.append("tmp.c").append(aliasMapping.size() + i);
                    }

                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported update join style: " + dbmsDialect.getUpdateJoinStyle());
            }
        }

        StringBuilder withClause = applyCtes(sb, baseQuery, participatingQueries);
        // NOTE: CTEs will only be added, if this is a subquery
        Map<String, String> addedCtes = applyExtendedSql(sb, false, isEmbedded, withClause, tableToUpdate, returningColumns, includedModificationStates);
        participatingQueries.add(baseQuery);
        participatingQueries.add(exampleQuery);
        participatingQueries.add(updateExampleQuery);
        participatingQueries.addAll(foreignKeyParticipatingQueries);

        boolean hasCtes = withClause != null && withClause.length() != 0 || addedCtes != null && !addedCtes.isEmpty();
        if (hasCtes && returningAttributeBindingMap.isEmpty() && !dbmsDialect.usesExecuteUpdateWhenWithClauseInModificationQuery()) {
            query = exampleQuery;
        } else {
            query = updateExampleQuery;
        }

        this.sql = sb.toString();
        this.participatingQueries = participatingQueries;
        this.addedCtes = addedCtes;
        this.dirty = false;
    }
}
