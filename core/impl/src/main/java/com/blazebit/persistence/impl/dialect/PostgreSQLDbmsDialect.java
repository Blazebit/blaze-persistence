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

package com.blazebit.persistence.impl.dialect;

import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.blazebit.persistence.spi.DbmsLimitHandler;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.SetOperationType;

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
    public Map<String, String> appendExtendedSql(StringBuilder sqlSb, DbmsStatementType statementType, boolean isSubquery, boolean isEmbedded, StringBuilder withClause, String limit, String offset, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
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
                sqlSb.append("\nunion\n");
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
        if (limit != null) {
            appendLimit(sqlSb, isSubquery, limit, offset);
        }
        
        if (returningColumns != null) {
            sqlSb.append(" returning ");

            for (int i = 0; i < returningColumns.length; i++) {
                if (i != 0) {
                    sqlSb.append(",");
                }

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
                sqlSb.append("\n");
                sqlSb.append(operator);
                sqlSb.append("\n");
            }

            if (hasOuterClause && !operand.startsWith("(")) {
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
            String needle = "from";
            int startIndex = indexOfIgnoreCase(sb, needle) + needle.length() + 1;
            int endIndex = sb.indexOf(" ", startIndex);
            return sb.substring(startIndex, endIndex);
        } else if (statementType == DbmsStatementType.UPDATE) {
            String needle = "update";
            int startIndex = indexOfIgnoreCase(sb, needle) + needle.length() + 1;
            int endIndex = sb.indexOf(" ", startIndex);
            return sb.substring(startIndex, endIndex);
        } else if (statementType == DbmsStatementType.INSERT) {
            String needle = "into";
            int startIndex = indexOfIgnoreCase(sb, needle) + needle.length() + 1;
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
    
}
