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
import com.blazebit.persistence.impl.plan.CustomSelectQueryPlan;
import com.blazebit.persistence.impl.plan.ModificationQueryPlan;
import com.blazebit.persistence.impl.plan.SelectQueryPlan;
import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.persistence.spi.ServiceProvider;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CustomQuerySpecification<T> implements QuerySpecification<T> {

    protected final EntityManager em;
    protected final DbmsDialect dbmsDialect;
    protected final ServiceProvider serviceProvider;
    protected final ExtendedQuerySupport extendedQuerySupport;

    protected final DbmsStatementType statementType;
    protected final Query baseQuery;
    protected final Set<Parameter<?>> parameters;
    protected final Map<String, Collection<?>> listParameters;
    protected final String limit;
    protected final String offset;

    protected final List<String> keyRestrictedLeftJoinAliases;
    protected final List<EntityFunctionNode> entityFunctionNodes;
    protected final boolean recursive;
    protected final List<CTENode> ctes;
    protected final boolean shouldRenderCtes;

    protected boolean dirty;
    protected String sql;
    protected List<Query> participatingQueries;
    protected Map<String, String> addedCtes;

    public CustomQuerySpecification(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> commonQueryBuilder, Query baseQuery, Set<Parameter<?>> parameters, Set<String> listParameters, String limit, String offset,
                                    List<String> keyRestrictedLeftJoinAliases, List<EntityFunctionNode> entityFunctionNodes, boolean recursive, List<CTENode> ctes, boolean shouldRenderCtes) {
        this.em = commonQueryBuilder.getEntityManager();
        this.dbmsDialect = commonQueryBuilder.getService(DbmsDialect.class);
        this.serviceProvider = commonQueryBuilder;
        this.extendedQuerySupport = commonQueryBuilder.getService(ExtendedQuerySupport.class);
        this.statementType = commonQueryBuilder.getStatementType();
        this.baseQuery = baseQuery;
        this.parameters = parameters;
        this.listParameters = new HashMap<>();
        this.limit = limit;
        this.offset = offset;

        for (String listParameter : listParameters) {
            this.listParameters.put(listParameter, Collections.emptyList());
        }

        this.keyRestrictedLeftJoinAliases = keyRestrictedLeftJoinAliases;
        this.entityFunctionNodes = entityFunctionNodes;
        this.recursive = recursive;
        this.ctes = ctes;
        this.shouldRenderCtes = shouldRenderCtes;
        this.dirty = true;
    }

    @Override
    public ModificationQueryPlan createModificationPlan(int firstResult, int maxResults) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SelectQueryPlan<T> createSelectPlan(int firstResult, int maxResults) {
        final String sql = getSql();
        return new CustomSelectQueryPlan<>(extendedQuerySupport, serviceProvider, baseQuery, participatingQueries, sql, firstResult, maxResults);
    }

    @Override
    public String getSql() {
        if (dirty) {
            initialize();
        }
        return sql;
    }

    @Override
    public List<Query> getParticipatingQueries() {
        if (dirty) {
            initialize();
        }
        return participatingQueries;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Parameter<?>> getParameters() {
        return (Set<Parameter<?>>) (Set) parameters;
    }

    @Override
    public Map<String, String> getAddedCtes() {
        if (dirty) {
            initialize();
        }
        return addedCtes;
    }

    @Override
    public Query getBaseQuery() {
        return baseQuery;
    }

    @Override
    public void onCollectionParameterChange(String parameterName, Collection<?> value) {
        Collection<?> listParameterValue = listParameters.get(parameterName);
        if (listParameterValue != null && listParameterValue.size() != value.size()) {
            dirty = true;
            listParameters.put(parameterName, value);
        }
    }

    protected void initialize() {
        List<Query> participatingQueries = new ArrayList<>();

        for (Map.Entry<String, Collection<?>> entry : listParameters.entrySet()) {
            baseQuery.setParameter(entry.getKey(), entry.getValue());
        }

        String sqlQuery = extendedQuerySupport.getSql(em, baseQuery);
        StringBuilder sqlSb = applySqlTransformations(sqlQuery);
        StringBuilder withClause = applyCtes(sqlSb, baseQuery, participatingQueries);
        Map<String, String> addedCtes = applyExtendedSql(sqlSb, false, false, withClause, null, null);
        participatingQueries.add(baseQuery);

        this.sql = sqlSb.toString();
        this.participatingQueries = participatingQueries;
        this.addedCtes = addedCtes;
        this.dirty = false;
    }

    protected Map<String, String> applyExtendedSql(StringBuilder sqlSb, boolean isSubquery, boolean isEmbedded, StringBuilder withClause, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
        return dbmsDialect.appendExtendedSql(sqlSb, statementType, isSubquery, isEmbedded, withClause, limit, offset, returningColumns, includedModificationStates);
    }

    protected StringBuilder applyCtes(StringBuilder sqlSb, Query baseQuery, List<Query> participatingQueries) {
        // When we shouldn't render CTEs and there aren't user defined ones, we don't render anything
        if (!shouldRenderCtes || (ctes.isEmpty() && (statementType != DbmsStatementType.DELETE || !dbmsDialect.supportsModificationQueryInWithClause()))) {
            // But delete statements could contribute cascading deletes, so we try to apply these
            // Skip other statement types or if we have a delete but the DBMS doesn't support modification queries in the with clause
            return null;
        }
        // EntityAlias -> CteName
        Map<String, String> tableNameRemapping = new LinkedHashMap<String, String>(0);

        StringBuilder sb = new StringBuilder(ctes.size() * 100);
        sb.append(dbmsDialect.getWithClause(recursive));
        sb.append(" ");

        boolean firstCte = true;
        for (CTENode cteInfo : ctes) {
            // Build queries and add as participating queries
            QuerySpecification<?> nonRecursiveQuerySpecification = cteInfo.getNonRecursiveQuerySpecification();
            Query nonRecursiveQuery = nonRecursiveQuerySpecification.getBaseQuery();
            participatingQueries.addAll(nonRecursiveQuerySpecification.getParticipatingQueries());

            QuerySpecification<?> recursiveQuerySpecification = null;
            if (cteInfo.isRecursive()) {
                recursiveQuerySpecification = cteInfo.getRecursiveQuerySpecification();
                participatingQueries.addAll(recursiveQuerySpecification.getParticipatingQueries());
            }

            if (dbmsDialect.supportsModificationQueryInWithClause()) {
                // add cascading delete statements as CTEs
                firstCte = applyCascadingDelete(nonRecursiveQuery, participatingQueries, sb, cteInfo.getName(), firstCte);
            }

            firstCte = applyAddedCtes(nonRecursiveQuerySpecification, cteInfo.getNonRecursiveTableNameRemappings(), sb, tableNameRemapping, firstCte);
            firstCte = applyAddedCtes(recursiveQuerySpecification, cteInfo.getRecursiveTableNameRemappings(), sb, tableNameRemapping, firstCte);

            if (firstCte) {
                firstCte = false;
            } else {
                sb.append(",\n");
            }

            sb.append(cteInfo.getHead());
            sb.append(" AS(\n");

            final String sql = cteInfo.getNonRecursiveQuerySpecification().getSql();

            if (cteInfo.getAliases() != null) {
                // This code path is only relevant for oracle 10g because back then aliases were taken from the select aliases of the query
                final String[] newAliases = cteInfo.getAliases();
                final StringBuilder newSqlSb = new StringBuilder(sql.length());
                String[] endPositions = SqlUtils.getSelectItems(sql, 0, new SqlUtils.SelectItemExtractor() {
                    @Override
                    public String extract(StringBuilder sb, int index, int currentPosition) {
                        if (index == 0) {
                            newSqlSb.append(sql, 0, currentPosition - sb.length());
                        } else {
                            newSqlSb.append(',');
                        }

                        String originalAlias = SqlUtils.extractAlias(sb);
                        int aliasPosition = sb.length() - originalAlias.length() - 1;
                        // Replace the original alias with the new one
                        if (aliasPosition != -1 && sb.charAt(aliasPosition) == ' ') {
                            newSqlSb.append(sb, 0, aliasPosition + 1);
                        } else {
                            // Append the new alias
                            newSqlSb.append(sb);
                            newSqlSb.append(" as ");
                        }

                        newSqlSb.append(newAliases[index]);

                        return Integer.toString(currentPosition);
                    }
                });

                newSqlSb.append(sql, Integer.valueOf(endPositions[endPositions.length - 1]), sql.length());
                sb.append(newSqlSb);
            } else {
                sb.append(sql);
            }

            if (cteInfo.isRecursive()) {
                if (cteInfo.isUnionAll()) {
                    sb.append("\nUNION ALL\n");
                } else {
                    sb.append("\nUNION\n");
                }
                sb.append(cteInfo.getRecursiveQuerySpecification().getSql());
            } else if (!dbmsDialect.supportsNonRecursiveWithClause()) {
                sb.append(cteInfo.getNonRecursiveWithClauseSuffix());
            }

            sb.append("\n)");
        }

        if (dbmsDialect.supportsModificationQueryInWithClause()) {
            // Add cascading delete statements from base query as CTEs
            firstCte = applyCascadingDelete(baseQuery, participatingQueries, sb, "main_query", firstCte);
        }

        // If no CTE has been added, we can just return
        if (firstCte) {
            return null;
        }

        for (CTENode cteInfo : ctes) {
            String cteName = cteInfo.getEntityName();
            // TODO: this is a hibernate specific integration detail
            // Replace the subview subselect that is generated for this cte
            final String subselect = "( select * from " + cteName + " )";
            int subselectIndex = 0;
            while ((subselectIndex = sb.indexOf(subselect, subselectIndex)) > -1) {
                sb.replace(subselectIndex, subselectIndex + subselect.length(), cteName);
            }

            final String mainSubselect = "( select * from " + cteName + " )";
            subselectIndex = 0;
            while ((subselectIndex = sqlSb.indexOf(mainSubselect, subselectIndex)) > -1) {
                sqlSb.replace(subselectIndex, subselectIndex + mainSubselect.length(), cteName);
            }
        }

        sb.append("\n");

        for (Map.Entry<String, String> tableNameRemappingEntry : tableNameRemapping.entrySet()) {
            String sqlAlias = extendedQuerySupport.getSqlAlias(em, baseQuery, tableNameRemappingEntry.getKey());
            String newCteName = tableNameRemappingEntry.getValue();

            SqlUtils.applyTableNameRemapping(sqlSb, sqlAlias, newCteName, null);
        }

        return sb;
    }

    private boolean applyAddedCtes(QuerySpecification<?> querySpecification, Map<String, String> cteTableNameRemappings, StringBuilder sb, Map<String, String> tableNameRemapping, boolean firstCte) {
        if (querySpecification != null) {
            // CteName -> CteQueryString
            Map<String, String> addedCtes = querySpecification.getAddedCtes();
            if (addedCtes != null && addedCtes.size() > 0) {
                for (Map.Entry<String, String> simpleCteEntry : addedCtes.entrySet()) {
                    for (Map.Entry<String, String> cteTableNameRemapping : cteTableNameRemappings.entrySet()) {
                        if (cteTableNameRemapping.getValue().equals(simpleCteEntry.getKey())) {
                            tableNameRemapping.put(cteTableNameRemapping.getKey(), cteTableNameRemapping.getValue());
                        }
                    }

                    if (firstCte) {
                        firstCte = false;
                    } else {
                        sb.append(",\n");
                    }

                    sb.append(simpleCteEntry.getKey());
                    sb.append(" AS (\n");
                    sb.append(simpleCteEntry.getValue());
                    sb.append("\n)");
                }
            }
        }

        return firstCte;
    }

    private boolean applyCascadingDelete(Query baseQuery, List<Query> participatingQueries, StringBuilder sb, String cteBaseName, boolean firstCte) {
        List<String> cascadingDeleteSqls = extendedQuerySupport.getCascadingDeleteSql(em, baseQuery);
        StringBuilder cascadingDeleteSqlSb = new StringBuilder();
        int cteBaseNameCount = 0;
        for (String cascadingDeleteSql : cascadingDeleteSqls) {
            if (firstCte) {
                firstCte = false;
            } else {
                sb.append(",\n");
            }

            // Since we kind of need the parameters from the base query, it will participate for each cascade
            participatingQueries.add(baseQuery);

            sb.append(cteBaseName);
            sb.append('_').append(cteBaseNameCount++);
            sb.append(" AS (\n");

            cascadingDeleteSqlSb.setLength(0);
            cascadingDeleteSqlSb.append(cascadingDeleteSql);
            dbmsDialect.appendExtendedSql(cascadingDeleteSqlSb, DbmsStatementType.DELETE, false, true, null, null, null, null, null);
            sb.append(cascadingDeleteSqlSb);

            sb.append("\n)");
        }

        return firstCte;
    }

    protected StringBuilder applySqlTransformations(String sqlQuery) {
        if (entityFunctionNodes.isEmpty() && keyRestrictedLeftJoinAliases.isEmpty()) {
            return new StringBuilder(sqlQuery);
        }

        // TODO: find a better size estimate
        StringBuilder sb = new StringBuilder(sqlQuery.length() +
                // Just a stupid estimate
                entityFunctionNodes.size() * 100 +
                // we put "(select * from )" around
                keyRestrictedLeftJoinAliases.size() * 20);
        sb.append(sqlQuery);

        for (String sqlAlias : keyRestrictedLeftJoinAliases) {
            applyLeftJoinSubqueryRewrite(sb, sqlAlias);
        }

        for (EntityFunctionNode node : entityFunctionNodes) {
            String valuesTableSqlAlias = node.getTableAlias();
            String valuesClause = node.getValuesClause();
            String valuesAliases = node.getValuesAliases();
            String syntheticPredicate = node.getSyntheticPredicate();

            // TODO: this is a hibernate specific integration detail
            // Replace the subview subselect that is generated for this subselect
            String entityName = node.getEntityClass().getSimpleName();
            final String subselect = "( select * from " + entityName + " )";
            int subselectIndex = sb.indexOf(subselect, 0);
            if (subselectIndex == -1) {
                // this is probably a VALUES clause for an entity type
                int syntheticPredicateStart = sb.indexOf(syntheticPredicate, SqlUtils.indexOfWhere(sb));
                sb.replace(syntheticPredicateStart, syntheticPredicateStart + syntheticPredicate.length(), "1=1");
            } else {
                while ((subselectIndex = sb.indexOf(subselect, subselectIndex)) > -1) {
                    int endIndex = subselectIndex + subselect.length();
                    int syntheticPredicateStart = sb.indexOf(syntheticPredicate, endIndex);
                    sb.replace(syntheticPredicateStart, syntheticPredicateStart + syntheticPredicate.length(), "1=1");
                    sb.replace(subselectIndex, endIndex, entityName);
                }
            }

            SqlUtils.applyTableNameRemapping(sb, valuesTableSqlAlias, valuesClause, valuesAliases);
        }

        return sb;
    }

    private void applyLeftJoinSubqueryRewrite(StringBuilder sb, String sqlAlias) {
        final String searchAs = " as";
        final String searchAlias = " " + sqlAlias;
        int searchIndex = 0;
        while ((searchIndex = sb.indexOf(searchAlias, searchIndex)) > -1) {
            char c = sb.charAt(searchIndex + searchAlias.length());
            if (c == '.') {
                // This is a dereference of the alias, skip this
            } else {
                int[] indexRange;
                if (searchAs.equalsIgnoreCase(sb.substring(searchIndex - searchAs.length(), searchIndex))) {
                    // Uses aliasing with the AS keyword
                    indexRange = SqlUtils.rtrimBackwardsToFirstWhitespace(sb, searchIndex - searchAs.length());
                } else {
                    // Uses aliasing without the AS keyword
                    indexRange = SqlUtils.rtrimBackwardsToFirstWhitespace(sb, searchIndex);
                }

                // Jump back two left joins to further inspect the join table
                String leftJoinString = "left outer join ";
                int joinTableJoinIndex = -1;
                int targetTableJoinIndex = -1;
                int currentIndex = -1;
                while ((currentIndex = sb.indexOf(leftJoinString, currentIndex + 1)) < indexRange[0] && currentIndex > 0) {
                    joinTableJoinIndex = targetTableJoinIndex;
                    targetTableJoinIndex = currentIndex;
                }

                if (joinTableJoinIndex < 1) {
                    throw new IllegalStateException("The left join for subquery rewriting could not be found!");
                }

                int joinTableIndex = joinTableJoinIndex + leftJoinString.length();

                // Extract the on condition so we can move it
                String onString = " on ";
                int onIndex = sb.indexOf(onString, joinTableIndex);

                if (onIndex > targetTableJoinIndex) {
                    throw new IllegalStateException("The left join for subquery rewriting could not be found!");
                }
                StringBuilder onCondition = new StringBuilder(sb.substring(onIndex, targetTableJoinIndex));

                // Extract the join table alias since we need to replace it
                int aliasIndex = sb.indexOf(" ", joinTableIndex) + 1;
                String joinTableAlias = sb.substring(aliasIndex, onIndex);

                int realOnConditionStartIndex = indexRange[1];
                // Find the index at which the actual key restriction begins
                String realOnConditionStart = " and (";
                int realOnConditionIndex = sb.indexOf(realOnConditionStart, realOnConditionStartIndex);

                // We need to find the column name of the key
                List<String> joinTableParentExpressions = getColumnExpressions(sb, joinTableAlias, onIndex, targetTableJoinIndex);
                List<String> joinTableKeyExpressions = getColumnExpressions(sb, joinTableAlias, realOnConditionIndex, sb.length());

                if (joinTableKeyExpressions.size() != 1) {
                    throw new IllegalStateException("Expected exactly one key expression but found: " + joinTableKeyExpressions.size());
                }

                String joinTableKeyExpression = joinTableKeyExpressions.get(0);

                // Construct the subquery part that will replace the join table join part
                String joinTableKeyAlias = "join_table_key";
                String joinTableParentAliasPrefix = "join_table_parent_";
                StringBuilder subqueryPrefixSb = new StringBuilder();
                subqueryPrefixSb.append("(select ");
                subqueryPrefixSb.append(joinTableKeyExpression);
                subqueryPrefixSb.append(" as ");
                subqueryPrefixSb.append(joinTableKeyAlias);
                subqueryPrefixSb.append(", ");
                subqueryPrefixSb.append(sqlAlias);
                subqueryPrefixSb.append(".*");

                for (int i = 0; i < joinTableParentExpressions.size(); i++) {
                    subqueryPrefixSb.append(", ");
                    subqueryPrefixSb.append(joinTableParentExpressions.get(i));
                    subqueryPrefixSb.append(" as ");
                    subqueryPrefixSb.append(joinTableParentAliasPrefix);
                    subqueryPrefixSb.append(i);

                    String newParentExpression = sqlAlias + "." + joinTableParentAliasPrefix + i;
                    int lengthDifference = newParentExpression.length() - joinTableParentExpressions.get(i).length();
                    replaceExpressionUntil(0, onCondition.length(), lengthDifference, onCondition, joinTableParentExpressions.get(i), newParentExpression);
                }

                subqueryPrefixSb.append(" from ");

                // Replace the join table join with a subquery part
                String subqueryPrefix =  subqueryPrefixSb.toString();
                String subqueryInsert = subqueryPrefix + sb.substring(joinTableIndex, onIndex);
                sb.replace(joinTableIndex, targetTableJoinIndex - 1, subqueryInsert);

                // Adapt index since we replaced stuff before
                realOnConditionStartIndex += (subqueryInsert.length() - (targetTableJoinIndex - joinTableIndex));
                realOnConditionIndex += (subqueryInsert.length() - (targetTableJoinIndex - joinTableIndex - 1));

                // Insert the target table alias for the subquery and the on condition for joining with the parent
                String subqueryEnd = ") " + sqlAlias + onCondition;
                sb.insert(realOnConditionIndex, subqueryEnd);
                realOnConditionStartIndex += subqueryEnd.length();

                // Replace the join table key expression with the target table key expression until reaching joinTableIndex and then again at realOnConditionStartIndex
                String targetTableKeyExpression = sqlAlias + "." + joinTableKeyAlias;
                int lengthDifference = targetTableKeyExpression.length() - joinTableKeyExpression.length();
                // Replace the join table alias with the target table alias until reaching joinTableIndex
                int diff = replaceExpressionUntil(-1, joinTableIndex, lengthDifference, sb, joinTableKeyExpression, targetTableKeyExpression);
                // and then again from realOnConditionStartIndex until the end
                replaceExpressionUntil(realOnConditionStartIndex + diff, sb.length(), lengthDifference, sb, joinTableKeyExpression, targetTableKeyExpression);

                break;
            }

            searchIndex = searchIndex + 1;
        }
    }

    private List<String> getColumnExpressions(StringBuilder sb, String tableAlias, int startIndex, int endIndex) {
        String columnExpressionStart = tableAlias + ".";
        List<String> columnExpressions = new ArrayList<String>();
        while (startIndex < endIndex) {
            int expressionIndex = sb.indexOf(columnExpressionStart, startIndex);

            if (expressionIndex < 0) {
                if (columnExpressions.isEmpty()) {
                    throw new IllegalStateException("The join table column expression needed for subquery rewriting could not be found!");
                }
                break;
            }

            StringBuilder columnExpressionSb = new StringBuilder(80);
            columnExpressionSb.append(columnExpressionStart);
            expressionIndex += columnExpressionStart.length();
            char keyChar;
            while (SqlUtils.isIdentifier(keyChar = sb.charAt(expressionIndex))) {
                columnExpressionSb.append(keyChar);
                expressionIndex++;
            }
            columnExpressions.add(columnExpressionSb.toString());
            startIndex = expressionIndex;
        }

        return columnExpressions;
    }

    private int replaceExpressionUntil(int searchIndex, int endIndex, int lengthDifference, StringBuilder sb, String oldExpression, String newExpression) {
        int diff = 0;
        while ((searchIndex = sb.indexOf(oldExpression, searchIndex + 1)) > 0 && searchIndex < endIndex) {
            if (SqlUtils.isIdentifierStart(sb.charAt(searchIndex - 1)) || SqlUtils.isIdentifier(sb.charAt(searchIndex + oldExpression.length()))) {
                continue;
            }
            sb.replace(searchIndex, searchIndex + oldExpression.length(), newExpression);
            searchIndex += lengthDifference;
            endIndex += lengthDifference;
            diff += lengthDifference;
        }
        return diff;
    }

    private String getSql(Query query) {
        if (query instanceof CustomSQLQuery) {
            return ((CustomSQLQuery) query).getSql();
        } else if (query instanceof CustomSQLTypedQuery<?>) {
            return ((CustomSQLTypedQuery<?>) query).getSql();
        }
        return extendedQuerySupport.getSql(em, query);
    }

}
