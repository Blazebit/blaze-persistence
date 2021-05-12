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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.DbmsLimitHandler;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.DeleteJoinStyle;
import com.blazebit.persistence.spi.OrderByElement;
import com.blazebit.persistence.spi.UpdateJoinStyle;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DB2DbmsDialect extends DefaultDbmsDialect {

    public DB2DbmsDialect() {
        super(getSqlTypes());
    }

    public DB2DbmsDialect(Map<Class<?>, String> childSqlTypes) {
        super(childSqlTypes);
    }

    protected static Map<Class<?>, String> getSqlTypes() {
        Map<Class<?>, String> types = new HashMap<Class<?>, String>();

        // We have to specify a length and we just choose 2048 because it will most probably be a good fit
        types.put(String.class, "varchar(2048)");

        return types;
    }

    @Override
    public boolean supportsAnsiRowValueConstructor() {
        // At least Hibernate thinks so. We need this to get embeddable splitting working
        return false;
    }

    @Override
    public String getWithClause(boolean recursive) {
        return "with";
    }

    @Override
    public DeleteJoinStyle getDeleteJoinStyle() {
        return DeleteJoinStyle.MERGE;
    }

    @Override
    public UpdateJoinStyle getUpdateJoinStyle() {
        return UpdateJoinStyle.MERGE;
    }

    @Override
    public boolean supportsComplexJoinOn() {
        return false;
    }

    @Override
    public boolean supportsJoinsInRecursiveCte() {
        // See https://www.ibm.com/support/knowledgecenter/SSEPEK_10.0.0/com.ibm.db2z10.doc.codes/src/tpc/n345.dita
        return false;
    }

    @Override
    public boolean supportsReturningColumns() {
        return true;
    }

    @Override
    public boolean supportsModificationQueryInWithClause() {
        return true;
    }
    
    @Override
    public boolean usesExecuteUpdateWhenWithClauseInModificationQuery() {
        return false;
    }

    @Override
    public boolean supportsIntersect(boolean all) {
        // Supported since 9.7 http://www.ibm.com/support/knowledgecenter/SSEPGG_9.7.0/com.ibm.db2.luw.sql.ref.doc/doc/r0000877.html
        return true;
    }

    @Override
    public boolean supportsExcept(boolean all) {
        // Supported since 9.7 http://www.ibm.com/support/knowledgecenter/SSEPGG_9.7.0/com.ibm.db2.luw.sql.ref.doc/doc/r0000877.html
        return true;
    }

    @Override
    protected boolean supportsPartitionInRowNumberOver() {
        return true;
    }

    @Override
    public boolean supportsArbitraryLengthMultiset() {
        return true;
    }

    @Override
    public Map<String, String> appendExtendedSql(StringBuilder sqlSb, DbmsStatementType statementType, boolean isSubquery, boolean isEmbedded, StringBuilder withClause, String limit, String offset, String dmlAffectedTable, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
        // since changes in DB2 will be visible to other queries, we need to preserve the old state if required
        boolean requiresOld = includedModificationStates != null && includedModificationStates.containsKey(DbmsModificationState.OLD);
        boolean addParenthesis = isSubquery && sqlSb.length() > 0 && sqlSb.charAt(0) != '(';
        
        if (requiresOld) {
            Map<String, String> dbmsModificationStateQueries = new LinkedHashMap<String, String>();
            StringBuilder sb = new StringBuilder(sqlSb.length() + 30);
            if (statementType == DbmsStatementType.INSERT) {
                StringBuilder newValuesSb = new StringBuilder();
                String newValuesTableName = includedModificationStates.get(DbmsModificationState.OLD) + "_new";
                newValuesSb.append("select * from final table (");
                newValuesSb.append(sqlSb);
                newValuesSb.append(")");
                dbmsModificationStateQueries.put(newValuesTableName, newValuesSb.toString());
                
                int startIndex = SqlUtils.INTO_FINDER.indexIn(sqlSb, 0, sqlSb.length()) + SqlUtils.INTO.length();
                int endIndex = sqlSb.indexOf(" ", startIndex);
                endIndex = indexOfOrEnd(sqlSb, '(', startIndex, endIndex);
                String table = sqlSb.substring(startIndex, endIndex);

                sb.append("select * from ");
                sb.append(table);
                sb.append(" except ");
                sb.append("select * from ");
                sb.append(newValuesTableName);
            } else {
                sb.append("select * from old table (");
                sb.append(sqlSb);
                sb.append(")");
            }

            sqlSb.setLength(0);
            
            if (addParenthesis) {
                sqlSb.append('(');
            }
            
            sqlSb.append("select ");
            for (int i = 0; i < returningColumns.length; i++) {
                if (i != 0) {
                    sqlSb.append(',');
                }
                sqlSb.append(returningColumns[i]);
            }
            
            sqlSb.append(" from ");
            sqlSb.append(includedModificationStates.get(DbmsModificationState.OLD));
            
            dbmsModificationStateQueries.put(includedModificationStates.get(DbmsModificationState.OLD), sb.toString());
            
            if (addParenthesis) {
                sqlSb.append(')');
            }
            
            return dbmsModificationStateQueries;
        }
        
        boolean needsReturningWrapper = statementType != DbmsStatementType.SELECT && (isEmbedded || returningColumns != null);
        if (needsReturningWrapper || withClause != null && (statementType != DbmsStatementType.SELECT)) {
            if (addParenthesis) {
                sqlSb.insert(0, '(');
            }
            
            // Insert might need limit
            if (limit != null) {
                appendLimit(sqlSb, isSubquery, limit, offset);
            }
            
            String[] columns;
            if (returningColumns == null) {
                // we will simulate the update count
                columns = new String[]{ "count(*)" };
            } else {
                columns = returningColumns;
            }
            
            if (needsReturningWrapper) {
                applyQueryReturning(sqlSb, statementType, withClause, columns);
            } else {
                applyQueryReturning(sqlSb, statementType, withClause, columns);
            }
            
            if (addParenthesis) {
                sqlSb.append(')');
            }
            
            return null;
        }

        if (addParenthesis) {
            sqlSb.insert(0, '(');
        }
        
        // This is a select
        if (withClause != null) {
            sqlSb.insert(SqlUtils.SELECT_FINDER.indexIn(sqlSb, 0, sqlSb.length()), withClause);
        }
        if (limit != null) {
            appendLimit(sqlSb, isSubquery, limit, offset);
        }
        
        if (addParenthesis) {
            sqlSb.append(')');
        }
        
        return null;
    }

    @Override
    public DbmsLimitHandler createLimitHandler() {
        if (isCompatibilityVectorMYS()) {
            return super.createLimitHandler();
        }
        return new DB2DbmsLimitHandler();
    }

    protected boolean isCompatibilityVectorMYS() {
        // This requires DB2_COMPATIBILITY_VECTOR=MYS
        // See for reference: https://www.ibm.com/developerworks/community/blogs/SQLTips4DB2LUW/entry/limit_offset?lang=en
        return false;
    }

    @Override
    public boolean supportsNullPrecedence() {
        return false;
    }
    
    @Override
    public void appendOrderByElement(StringBuilder sqlSb, OrderByElement element, String[] aliases) {
        if (!element.isNullable() || (element.isNullsFirst() && !element.isAscending()) || (!element.isNullsFirst() && element.isAscending())) {
            // The following are ok according to DB2 docs
            // ASC + NULLS LAST
            // DESC + NULLS FIRST
            super.appendOrderByElement(sqlSb, element, aliases);
        } else {
            appendEmulatedOrderByElementWithNulls(sqlSb, element, aliases);
        }
    }

    private void applyQueryReturning(StringBuilder sqlSb, DbmsStatementType statementType, StringBuilder withClause, String[] returningColumns) {
        int initial = withClause != null ? withClause.length() : 0;
        StringBuilder sb = new StringBuilder(initial + 25 + returningColumns.length * 20);
        if (withClause != null) {
            sb.append(withClause);
        }
        
        sb.append("select ");
        for (int i = 0; i < returningColumns.length; i++) {
            if (i != 0) {
                sb.append(',');
            }
            sb.append(returningColumns[i]);
            sb.append(" as ret_col_");
            sb.append(i);
        }
        sb.append(" from ");
        
        if (statementType == DbmsStatementType.DELETE) {
            sb.append("old");
        } else {
            sb.append("final");
        }
        
        sb.append(" table (");
        sqlSb.insert(0, sb);
        sqlSb.append(')');
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
