package com.blazebit.persistence.impl.dialect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;

public class DefaultDbmsDialect implements DbmsDialect {

	@Override
	public boolean supportsWithClause() {
		return true;
	}

	@Override
	public boolean supportsNonRecursiveWithClause() {
		return true;
	}

	@Override
	public String getWithClause(boolean recursive) {
		if (recursive) {
			return "with recursive";
		} else {
			return "with";
		}
	}

	@Override
    public Map<String, String> appendExtendedSql(StringBuilder sqlSb, DbmsStatementType statementType, boolean isSubquery, StringBuilder withClause, String limit, String offset, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
	    if (withClause != null) {
	        sqlSb.insert(0, withClause);
	    }
        if (limit != null) {
            appendLimit(sqlSb, isSubquery, limit, offset);
        }
        if (isSubquery && !supportsReturningColumns() && returningColumns != null) {
            throw new IllegalArgumentException("Returning columns in a subquery is not possible for this dbms!");
        }
        
        return null;
    }

    @Override
    public boolean supportsWithClauseInModificationQuery() {
        return true;
    }

    @Override
    public boolean supportsModificationQueryInWithClause() {
        return false;
    }
    
    @Override
    public boolean usesExecuteUpdateWhenWithClauseInModificationQuery() {
        return true;
    }

    @Override
	public boolean supportsReturningGeneratedKeys() {
		return true;
	}

    @Override
    public boolean supportsReturningAllGeneratedKeys() {
        return true;
    }

	@Override
	public boolean supportsReturningColumns() {
		return false;
	}

    @Override
    public boolean supportsLimit() {
        return true;
    }

    @Override
    public boolean supportsLimitOffset() {
        return true;
    }

    public void appendLimit(StringBuilder sqlSb, boolean isSubquery, String limit, String offset) {
        if (offset == null) {
            sqlSb.append(" limit ").append(limit);
        } else {
            sqlSb.append(" limit ").append(limit).append(" offset ").append(offset);
        }
    }

    protected static String[] getSelectColumnAliases(String querySql) {
        int fromIndex = querySql.indexOf("from");
        int selectIndex = querySql.indexOf("select");
        String[] selectItems = splitSelectItems(querySql.subSequence(selectIndex + "select".length() + 1, fromIndex));
        String[] selectColumnAliases = new String[selectItems.length];
        
        for (int i = 0; i < selectItems.length; i++) {
            String selectItemWithAlias = selectItems[i].substring(selectItems[i].lastIndexOf('.') + 1);
            selectColumnAliases[i] = selectItemWithAlias.substring(selectItemWithAlias.lastIndexOf(' ') + 1);
        }
        
        return selectColumnAliases;
    }
    
    private static String[] splitSelectItems(CharSequence itemsString) {
        List<String> selectItems = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        int parenthesis = 0;
        boolean text = false;
        
        int i = 0;
        int length = itemsString.length();
        while (i < length) {
            char c = itemsString.charAt(i);
            
            if (text) {
                if (c == '(') {
                    parenthesis++;
                } else if (c == ')') {
                    parenthesis--;
                } else if (parenthesis == 0 && c == ',') {
                    selectItems.add(trim(sb));
                    sb.setLength(0);
                    text = false;
                    
                    i++;
                    continue;
                }
                
                sb.append(c);
            } else {
                if (Character.isWhitespace(c)) {
                    // skip whitespace
                } else {
                    sb.append(c);
                    text = true;
                }
            }
            
            i++;
        }
        
        if (text) {
            selectItems.add(trim(sb));
        }
        
        return selectItems.toArray(new String[selectItems.size()]);
    }

    private static String trim(StringBuilder sb) {
        int i = sb.length() - 1;
        while (i >= 0) {
            if (!Character.isWhitespace(sb.charAt(i))) {
                break;
            } else {
                i--;
            }
        }
        
        return sb.substring(0, i + 1);
    }

    protected static int indexOfIgnoreCase(StringBuilder haystack, String needle) {
        final int endLimit = haystack.length() - needle.length() + 1;
        for (int i = 0; i < endLimit; i++) {
            if (regionMatchesIgnoreCase(haystack, i, needle, 0, needle.length())) {
                return i;
            }
        }
        return -1;
    }

    protected static boolean regionMatchesIgnoreCase(StringBuilder haystack, int thisStart, String substring, int start, int length) {
       int index1 = thisStart;
       int index2 = start;
       int tmpLen = length;

       while (tmpLen-- > 0) {
           final char c1 = haystack.charAt(index1++);
           final char c2 = substring.charAt(index2++);

           if (c1 != c2 && Character.toUpperCase(c1) != Character.toUpperCase(c2) && Character.toLowerCase(c1) != Character.toLowerCase(c2)) {
               return false;
           }
       }

       return true;
    }

}
