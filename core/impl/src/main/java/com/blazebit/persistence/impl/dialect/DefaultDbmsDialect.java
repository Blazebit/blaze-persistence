package com.blazebit.persistence.impl.dialect;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;
import java.util.Date;

import com.blazebit.persistence.spi.*;

public class DefaultDbmsDialect implements DbmsDialect {

    private final Map<Class<?>, String> sqlTypes;

    public DefaultDbmsDialect() {
        this(Collections.EMPTY_MAP);
    }

    public DefaultDbmsDialect(Map<Class<?>, String> childSqlTypes) {
        Map<Class<?>, String> types = new HashMap<Class<?>, String>();

        types.put(Boolean.class, "boolean");
        types.put(Boolean.TYPE, "boolean");
        types.put(Byte.class, "tinyint");
        types.put(Byte.TYPE, "tinyint");
        types.put(Short.class, "smallint");
        types.put(Short.TYPE, "smallint");
        types.put(Integer.class, "integer");
        types.put(Integer.TYPE, "integer");
        types.put(Long.class, "bigint");
        types.put(Long.TYPE, "bigint");

        types.put(Float.class, "float");
        types.put(Float.TYPE, "float");
        types.put(Double.class, "double precision");
        types.put(Double.TYPE, "double precision");

        types.put(Character.class, "char");
        types.put(Character.TYPE, "char");

        types.put(String.class, "varchar");
        types.put(BigInteger.class, "bigint");
        types.put(BigDecimal.class, "decimal");
        types.put(Time.class, "time");
        types.put(java.sql.Date.class, "date");
        types.put(Timestamp.class, "timestamp");
        types.put(java.util.Date.class, "timestamp");
        types.put(java.util.Calendar.class, "timestamp");

        types.putAll(childSqlTypes);
        sqlTypes = Collections.unmodifiableMap(types);
    }

    @Override
    public boolean supportsTupleDistinctCounts() {
        return true;
    }

    @Override
	public boolean supportsWithClause() {
		return true;
	}

	@Override
	public boolean supportsNonRecursiveWithClause() {
		return true;
	}

    @Override
	public boolean supportsJoinsInRecursiveCte() {
	    return true;
	}

    @Override
    public String getSqlType(Class<?> castType) {
        return sqlTypes.get(castType);
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
    public Map<String, String> appendExtendedSql(StringBuilder sqlSb, DbmsStatementType statementType, boolean isSubquery, boolean isEmbedded, StringBuilder withClause, String limit, String offset, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
        if (isSubquery) {
            sqlSb.insert(0, '(');
        }

	    if (withClause != null) {
	        sqlSb.insert(0, withClause);
	    }
        if (limit != null) {
            appendLimit(sqlSb, isSubquery, limit, offset);
        }
        if (isSubquery && !supportsReturningColumns() && returningColumns != null) {
            throw new IllegalArgumentException("Returning columns in a subquery is not possible for this dbms!");
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
            boolean hasOuterClause = hasLimit || hasOrderBy;

            appendSetOperands(sqlSb, operator, isSubquery, operands, hasOuterClause);
            appendOrderBy(sqlSb, orderByElements);

            if (limit != null) {
                appendLimit(sqlSb, isSubquery, limit, offset);
            }
        }

        if (isSubquery) {
            sqlSb.append(')');
        }
    }

    protected void appendSetOperands(StringBuilder sqlSb, String operator, boolean isSubquery, List<String> operands, boolean hasOuterClause) {
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
    }

    protected void appendOrderBy(StringBuilder sqlSb, List<? extends OrderByElement> orderByElements) {
        if (orderByElements.isEmpty()) {
            return;
        }

        sqlSb.append(" order by ");
        boolean first = true;
        for (OrderByElement element : orderByElements) {
            if (first) {
                first = false;
            } else {
                sqlSb.append(',');
            }

            appendOrderByElement(sqlSb, element);
        }
    }

    protected void appendOrderByElement(StringBuilder sqlSb, OrderByElement element) {
        sqlSb.append(element.getPosition());

        if (element.isAscending()) {
            sqlSb.append(" asc");
        } else {
            sqlSb.append(" desc");
        }
        if (element.isNullsFirst()) {
            sqlSb.append(" nulls first");
        } else {
            sqlSb.append(" nulls last");
        }
    }

    protected void appendEmulatedOrderByElementWithNulls(StringBuilder sqlSb, OrderByElement element) {
        sqlSb.append("case when ");
        sqlSb.append(element.getPosition());
        sqlSb.append(" is null then ");
        sqlSb.append(element.isNullsFirst() ? 0 : 1);
        sqlSb.append(" else ");
        sqlSb.append(element.isNullsFirst() ? 1 : 0);
        sqlSb.append(" end, ");
        sqlSb.append(element.getPosition());
        sqlSb.append(element.isAscending() ? " asc" : " desc");
    }

    protected String getOperator(SetOperationType type) {
        if (type == null) {
            return null;
        }

        switch (type) {
            case UNION: return "UNION";
            case UNION_ALL: return "UNION ALL";
            case INTERSECT: return "INTERSECT";
            case INTERSECT_ALL: return "INTERSECT ALL";
            case EXCEPT: return "EXCEPT";
            case EXCEPT_ALL: return "EXCEPT ALL";
        }

        return null;
    }

    @Override
    public boolean supportsUnion(boolean all) {
        return true;
    }

    @Override
    public boolean supportsIntersect(boolean all) {
        // Most dbms don't support intersect all
        return !all;
    }

    @Override
    public boolean supportsExcept(boolean all) {
        // Most dbms don't support except all
        return !all;
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
	public boolean supportsComplexGroupBy() {
		return true;
	}

    @Override
    public boolean supportsComplexJoinOn() {
        return true;
    }

    @Override
    public ValuesStrategy getValuesStrategy() {
        return ValuesStrategy.VALUES;
    }

    @Override
    public String getDummyTable() {
        return null;
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
