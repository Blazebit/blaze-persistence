package com.blazebit.persistence.impl.dialect;

import java.util.Collections;
import java.util.Map;

import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;

public class PostgreSQLDbmsDialect extends DefaultDbmsDialect {

    @Override
    public boolean supportsModificationQueryInWithClause() {
        return true;
    }

	@Override
	public boolean supportsReturningColumns() {
		return true;
	}

    @Override
    public Map<String, String> appendExtendedSql(StringBuilder sqlSb, DbmsStatementType statementType, boolean isSubquery, StringBuilder withClause, String limit, String offset, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
        // since changes in PostgreSQL won't be visible to other queries, we need to create the new state if required
        boolean requiresNew = includedModificationStates != null && includedModificationStates.containsKey(DbmsModificationState.NEW);
        
        if (requiresNew) {
            StringBuilder sb = new StringBuilder(sqlSb.length() + returningColumns.length * 30);
            sb.append(sqlSb);
            sb.append(" returning *");
            
            sqlSb.setLength(0);
            
            if (statementType == DbmsStatementType.DELETE) {
                appendSelectColumnsFromTable(statementType, sb, sqlSb, returningColumns);
                sqlSb.append("\nexcept\n");
                appendSelectColumnsFromCte(sqlSb, returningColumns, includedModificationStates);
            } else {
                appendSelectColumnsFromCte(sqlSb, returningColumns, includedModificationStates);
                sqlSb.append("\nunion\n");
                appendSelectColumnsFromTable(statementType, sb, sqlSb, returningColumns);
            }
            
            return Collections.singletonMap(includedModificationStates.get(DbmsModificationState.NEW), sb.toString());
        }
        
        if (withClause != null) {
            sqlSb.insert(0, withClause);
        }
        if (limit != null) {
            appendLimit(sqlSb, isSubquery, limit, offset);
        }
        
        if (isSubquery && returningColumns != null) {
            sqlSb.append(" returning ");

            for (int i = 0; i < returningColumns.length; i++) {
                if (i != 0) {
                    sqlSb.append(",");
                }
                
                sqlSb.append(returningColumns[i]);
            }
        }
        
        return null;
    }
    
    private static void appendSelectColumnsFromCte(StringBuilder sqlSb, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
        sqlSb.append("select ");
        for (int i = 0; i < returningColumns.length; i++) {
            if (i != 0) {
                sqlSb.append(",");
            }
            
            sqlSb.append(returningColumns[i]);
        }
        
        sqlSb.append(" from ");
        sqlSb.append(includedModificationStates.get(DbmsModificationState.NEW));
    }
    
    private static void appendSelectColumnsFromTable(DbmsStatementType statementType, StringBuilder sb, StringBuilder sqlSb, String[] returningColumns) {
        String table;
        if (statementType == DbmsStatementType.DELETE) {
            String needle = "from";
            int startIndex = indexOfIgnoreCase(sb, needle) + needle.length() + 1;
            int endIndex = sb.indexOf(" ", startIndex);
            table = sb.substring(startIndex, endIndex);
        } else if (statementType == DbmsStatementType.UPDATE) {
            String needle = "update";
            int startIndex = indexOfIgnoreCase(sb, needle) + needle.length() + 1;
            int endIndex = sb.indexOf(" ", startIndex);
            table = sb.substring(startIndex, endIndex);
        } else if (statementType == DbmsStatementType.INSERT) {
            String needle = "into";
            int startIndex = indexOfIgnoreCase(sb, needle) + needle.length() + 1;
            int endIndex = sb.indexOf(" ", startIndex);
            endIndex = indexOfOrEnd(sb, '(', startIndex, endIndex);
            table = sb.substring(startIndex, endIndex);
        } else {
            throw new IllegalArgumentException("Unsupported statement type: " + statementType);
        }
        
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
