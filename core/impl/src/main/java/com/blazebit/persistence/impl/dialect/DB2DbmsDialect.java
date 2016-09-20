package com.blazebit.persistence.impl.dialect;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.OrderByElement;

public class DB2DbmsDialect extends DefaultDbmsDialect {

    public DB2DbmsDialect() {
        super(getSqlTypes());
    }

    private static Map<Class<?>, String> getSqlTypes() {
        Map<Class<?>, String> types = new HashMap<Class<?>, String>();

        // We have to specify a length and we just choose 2048 because it will most probably be a good fit
        types.put(String.class, "varchar(2048)");

        return types;
    }

    @Override
	public boolean supportsTupleDistinctCounts() {
		return false;
	}
    
	@Override
	public String getWithClause(boolean recursive) {
		return "with";
	}
	
	@Override
	public boolean supportsComplexGroupBy() {
		return false;
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
    public Map<String, String> appendExtendedSql(StringBuilder sqlSb, DbmsStatementType statementType, boolean isSubquery, boolean isEmbedded, StringBuilder withClause, String limit, String offset, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
        // since changes in DB2 will be visible to other queries, we need to preserve the old state if required
        boolean requiresOld = includedModificationStates != null && includedModificationStates.containsKey(DbmsModificationState.OLD);
        
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
                
                String needle = "into";
                int startIndex = indexOfIgnoreCase(sqlSb, needle) + needle.length() + 1;
                int endIndex = sqlSb.indexOf(" ", startIndex);
                endIndex = indexOfOrEnd(sqlSb, '(', startIndex, endIndex);
                String table = sqlSb.substring(startIndex, endIndex);

                sb.append("select * from ");
                sb.append(table);
                sb.append("\nexcept\n");
                sb.append("select * from ");
                sb.append(newValuesTableName);
            } else {
                sb.append("select * from old table (");
                sb.append(sqlSb);
                sb.append(")");
            }
            
            sqlSb.setLength(0);
            
            if (isSubquery) {
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
            
            if (isSubquery) {
                sqlSb.append(')');
            }
            
            return dbmsModificationStateQueries;
        }
        
        boolean needsReturningWrapper = isEmbedded && (returningColumns != null || statementType != DbmsStatementType.SELECT);
        if (needsReturningWrapper || withClause != null && (statementType != DbmsStatementType.SELECT)) {
            if (isSubquery) {
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
            
            if (isSubquery) {
                sqlSb.append(')');
            }
            
            return null;
        }

        if (isSubquery) {
            sqlSb.insert(0, '(');
        }
        
        // This is a select
        if (withClause != null) {
            sqlSb.insert(indexOfIgnoreCase(sqlSb, "select"), withClause);
        }
        if (limit != null) {
            appendLimit(sqlSb, isSubquery, limit, offset);
        }
        
        if (isSubquery) {
            sqlSb.append(')');
        }
        
        return null;
    }

    @Override
    public void appendLimit(StringBuilder sqlSb, boolean isSubquery, String limit, String offset) {
        if (offset == null) {
            sqlSb.append(" fetch first ").append(limit).append(" rows only");
        } else {
            // This requires DB2_COMPATIBILITY_VECTOR=MYS
            // See for reference: https://www.ibm.com/developerworks/community/blogs/SQLTips4DB2LUW/entry/limit_offset?lang=en
            sqlSb.append(" limit ").append(limit).append(" offset ").append(offset);
            // TODO: This is selecting the rownum too...
            // "select * from ( select db2limit2_.*, rownumber() over(order by order of db2limit2_) as rownumber_ from ( ?1 fetch first ?2 rows only ) as db2limit2_ ) as db2limit1_ where rownumber_ > ?3 order by rownumber_"
        }
    }
    
    @Override
    protected void appendSetOperands(StringBuilder sqlSb, String operator, boolean isSubquery, List<String> operands, boolean hasOuterClause) {
        if (!hasOuterClause) {
            super.appendSetOperands(sqlSb, operator, isSubquery, operands, hasOuterClause);
        } else {
            sqlSb.append("select * from (");
            super.appendSetOperands(sqlSb, operator, isSubquery, operands, hasOuterClause);
            sqlSb.append(')');
        }
    }
    
    @Override
    protected void appendOrderByElement(StringBuilder sqlSb, OrderByElement element) {
        if ((element.isNullsFirst() && !element.isAscending()) || (!element.isNullsFirst() && element.isAscending())) {
            // The following are ok according to DB2 docs
            // ASC + NULLS LAST
            // DESC + NULLS FIRST
            super.appendOrderByElement(sqlSb, element);
        } else {
            appendEmulatedOrderByElementWithNulls(sqlSb, element);
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
