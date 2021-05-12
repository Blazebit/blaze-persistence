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

package com.blazebit.persistence.impl.dialect;

import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.DbmsLimitHandler;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.DeleteJoinStyle;
import com.blazebit.persistence.spi.SetOperationType;
import com.blazebit.persistence.spi.UpdateJoinStyle;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PostgreSQLDbmsDialect extends DefaultDbmsDialect {

    public PostgreSQLDbmsDialect() {
    }

    public PostgreSQLDbmsDialect(Map<Class<?>, String> childSqlTypes) {
        super(childSqlTypes);
    }

    @Override
    public DeleteJoinStyle getDeleteJoinStyle() {
        return DeleteJoinStyle.USING;
    }

    @Override
    public UpdateJoinStyle getUpdateJoinStyle() {
        return UpdateJoinStyle.FROM;
    }

    @Override
    public boolean supportsModificationQueryInWithClause() {
        return true;
    }

    @Override
    public boolean supportsReturningColumns() {
        return true;
    }

    @Override
    public boolean supportsIntersect(boolean all) {
        return true;
    }

    @Override
    public boolean supportsExcept(boolean all) {
        return true;
    }

    @Override
    protected boolean supportsPartitionInRowNumberOver() {
        return true;
    }

    @Override
    public boolean supportsCountTuple() {
        return true;
    }

    @Override
    public boolean supportsFilterClause() {
        return false;
    }

    @Override
    public boolean supportsWindowNullPrecedence() {
        return true;
    }

    @Override
    protected boolean needsSetOperationWrapper() {
        return false;
    }

    @Override
    public int getPrepareFlags() {
        return Statement.NO_GENERATED_KEYS;
    }

    @Override
    public DbmsLimitHandler createLimitHandler() {
        return new PostgreSQLDbmsLimitHandler();
    }

    @Override
    public Map<String, String> appendExtendedSql(StringBuilder sqlSb, DbmsStatementType statementType, boolean isSubquery, boolean isEmbedded, StringBuilder withClause, String limit, String offset, String dmlAffectedTable, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
        // since changes in PostgreSQL won't be visible to other queries, we need to create the new state if required
        boolean requiresNew = includedModificationStates != null && includedModificationStates.containsKey(DbmsModificationState.NEW);
        boolean addParenthesis = isSubquery && sqlSb.length() > 0 && sqlSb.charAt(0) != '(';
        
        if (requiresNew) {
            StringBuilder newStateSb = new StringBuilder(sqlSb.length() + returningColumns.length * 30);
            if (statementType == DbmsStatementType.DELETE) {
                String deletedEntitiesCte = includedModificationStates.get(DbmsModificationState.NEW) + "_del";
                StringBuilder deleteSb = new StringBuilder(sqlSb.length() + returningColumns.length * 30);
                deleteSb.append(sqlSb);
                deleteSb.append(" returning *");

                // We move the actual delete and instead do a select from the deletedEntitiesCte
                sqlSb.setLength(0);
                appendSelectColumnsFromTable(sqlSb, returningColumns, deletedEntitiesCte);

                if (addParenthesis) {
                    newStateSb.append('(');
                }
                // The newState CTE will have all existing rows, except for the deleted ones
                appendSelectColumnsFromTable(newStateSb, new String[]{ "*" }, extractSingleTableName(statementType, deleteSb));
                newStateSb.append(" new_tmp_ where not exists (");
                appendSelectColumnsFromTable(newStateSb, new String[]{ "1" }, deletedEntitiesCte);
                newStateSb.append(" sub_tmp_ where (");
                for (int i = 0; i < returningColumns.length; i++) {
                    newStateSb.append("new_tmp_.").append(returningColumns[i]);
                    newStateSb.append(',');
                }
                // Close first tuple
                newStateSb.setCharAt(newStateSb.length() - 1, ')');

                newStateSb.append("=(");
                for (int i = 0; i < returningColumns.length; i++) {
                    newStateSb.append("sub_tmp_.").append(returningColumns[i]);
                    newStateSb.append(',');
                }
                // Close second tuple
                newStateSb.setCharAt(newStateSb.length() - 1, ')');

                // Close where exists subquery
                newStateSb.append(')');

                if (addParenthesis) {
                    newStateSb.append(')');
                }

                Map<String, String> addedCtes = new LinkedHashMap<>();
                addedCtes.put(deletedEntitiesCte, deleteSb.toString());
                addedCtes.put(includedModificationStates.get(DbmsModificationState.NEW), newStateSb.toString());
                return addedCtes;
            } else {
                newStateSb.append(sqlSb);
                newStateSb.append(" returning *");

                sqlSb.setLength(0);

                if (addParenthesis) {
                    sqlSb.append('(');
                }

                appendSelectColumnsFromTable(sqlSb, returningColumns, includedModificationStates.get(DbmsModificationState.NEW));
                sqlSb.append(" union ");
                appendSelectColumnsFromTable(sqlSb, returningColumns, extractSingleTableName(statementType, newStateSb));

                if (addParenthesis) {
                    sqlSb.append(')');
                }
            }

            return Collections.singletonMap(includedModificationStates.get(DbmsModificationState.NEW), newStateSb.toString());
        }

        if (addParenthesis) {
            sqlSb.insert(0, '(');
        }
        
        if (withClause != null) {
            sqlSb.insert(0, withClause);
        }
        if (limit != null || offset != null) {
            appendLimit(sqlSb, isSubquery, limit, offset);
        }
        
        if (returningColumns != null) {
            sqlSb.append(" returning ");

            for (int i = 0; i < returningColumns.length; i++) {
                if (i != 0) {
                    sqlSb.append(",");
                }

                sqlSb.append(dmlAffectedTable).append('.');
                sqlSb.append(returningColumns[i]);
            }
        }
        
        if (addParenthesis) {
            sqlSb.append(')');
        }
        
        return null;
    }
    
    @Override
    protected String[] appendSetOperands(StringBuilder sqlSb, SetOperationType setType, String operator, boolean isSubquery, List<String> operands, boolean hasOuterClause) {
        boolean first = true;
        for (String operand : operands) {
            if (first) {
                first = false;
            } else {
                sqlSb.append(' ');
                sqlSb.append(operator);
                sqlSb.append(' ');
            }

            // Need a wrapper for operands that have an order by clause
            if (hasOuterClause && !operand.startsWith("(") || SqlUtils.indexOfOrderBy(operand) != -1) {
                // Wrap operand so that the order by or limit has a clear target 
                sqlSb.append('(');
                sqlSb.append(operand);
                sqlSb.append(')');
            } else {
                sqlSb.append(operand);
            }
        }

        return null;
    }
    
    private static void appendSelectColumnsFromTable(StringBuilder sqlSb, String[] returningColumns, String table) {
        sqlSb.append(" select ");
        for (int i = 0; i < returningColumns.length; i++) {
            if (i != 0) {
                sqlSb.append(",");
            }

            sqlSb.append(returningColumns[i]);
        }
        sqlSb.append(" from ");
        sqlSb.append(table);
    }

    private static String extractSingleTableName(DbmsStatementType statementType, StringBuilder sb) {
        if (statementType == DbmsStatementType.DELETE) {
            int startIndex = SqlUtils.FROM_FINDER.indexIn(sb, 0, sb.length()) + SqlUtils.FROM.length();
            int endIndex = sb.indexOf(" ", startIndex);
            return sb.substring(startIndex, endIndex);
        } else if (statementType == DbmsStatementType.UPDATE) {
            int startIndex = SqlUtils.UPDATE_FINDER.indexIn(sb, 0, sb.length()) + SqlUtils.UPDATE.length();
            int endIndex = sb.indexOf(" ", startIndex);
            return sb.substring(startIndex, endIndex);
        } else if (statementType == DbmsStatementType.INSERT) {
            int startIndex = SqlUtils.INTO_FINDER.indexIn(sb, 0, sb.length()) + SqlUtils.INTO.length();
            int endIndex = sb.indexOf(" ", startIndex);
            endIndex = indexOfOrEnd(sb, '(', startIndex, endIndex);
            return sb.substring(startIndex, endIndex);
        } else {
            throw new IllegalArgumentException("Unsupported statement type: " + statementType);
        }
    }

    private static int indexOfOrEnd(StringBuilder sb, char needle, int startIndex, int endIndex) {
        while (startIndex < endIndex) {
            if (sb.charAt(startIndex) == needle) {
                return startIndex;
            }
            
            startIndex++;
        }
        
        return endIndex;
    }

    @Override
    public boolean supportsBooleanAggregation() {
        return true;
    }

    @Override
    public boolean supportsArbitraryLengthMultiset() {
        return true;
    }
}
