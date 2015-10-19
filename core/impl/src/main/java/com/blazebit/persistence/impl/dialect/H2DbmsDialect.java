package com.blazebit.persistence.impl.dialect;

import java.util.List;
import java.util.Map;

import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.OrderByElement;
import com.blazebit.persistence.spi.SetOperationType;

public class H2DbmsDialect extends DefaultDbmsDialect {
    
    @Override
    public boolean supportsReturningAllGeneratedKeys() {
        return false;
    }
    
	@Override
	public boolean supportsWithClause() {
		return true;
	}

	@Override
	public boolean supportsNonRecursiveWithClause() {
		return false;
	}

	@Override
	public String getWithClause(boolean recursive) {
		return "with recursive";
	}

    @Override
    public Map<String, String> appendExtendedSql(StringBuilder sqlSb, DbmsStatementType statementType, boolean isSubquery, boolean isEmbedded, StringBuilder withClause, String limit, String offset, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
        if (isSubquery) {
            sqlSb.insert(0, '(');
        }
        
        if (isSubquery && returningColumns != null) {
            throw new IllegalArgumentException("Returning columns in a subquery is not possible for this dbms!");
        }
        
        // NOTE: this only works for insert and select statements, but H2 does not support CTEs in modification queries anyway so it's ok
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
    public void appendSet(StringBuilder sqlSb, SetOperationType setType, boolean isSubquery, List<String> operands, List<? extends OrderByElement> orderByElements, String limit, String offset) {
        if (isSubquery) {
            sqlSb.insert(0, '(');
        }
        
        if (operands.size() > 0) {
            String operator = getOperator(setType);
            boolean hasLimit = limit != null;
            boolean hasOrderBy = orderByElements.size() > 0;
            boolean needWrapping = hasLimit || hasOrderBy;
            
            if (needWrapping) {
                sqlSb.append("select * from (");
            }
            
            boolean first = true;
            for (String operand : operands) {
                if (first) {
                    first = false;
                } else {
                    sqlSb.append("\n");
                    sqlSb.append(operator);
                    sqlSb.append("\n");
                }
    
                sqlSb.append(operand);
            }

            if (needWrapping) {
                sqlSb.append(')');
            }
            
            appendOrderBy(sqlSb, orderByElements);
            
            if (limit != null) {
                appendLimit(sqlSb, isSubquery, limit, offset);
            }
        }
        
        if (isSubquery) {
            sqlSb.append(')');
        }
    }

    @Override
    public boolean supportsWithClauseInModificationQuery() {
        return false;
    }

}
