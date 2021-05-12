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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.DbmsLimitHandler;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.DeleteJoinStyle;
import com.blazebit.persistence.spi.LateralStyle;
import com.blazebit.persistence.spi.OrderByElement;
import com.blazebit.persistence.spi.SetOperationType;
import com.blazebit.persistence.spi.UpdateJoinStyle;
import com.blazebit.persistence.spi.ValuesStrategy;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public class DefaultDbmsDialect implements DbmsDialect {

    private final Map<Class<?>, String> sqlTypes;

    public DefaultDbmsDialect() {
        this(Collections.EMPTY_MAP);
    }

    public DefaultDbmsDialect(Map<Class<?>, String> childSqlTypes) {
        Map<Class<?>, String> types = new HashMap<Class<?>, String>();

        types.put(Boolean.class, "number(1,0)");
        types.put(Boolean.TYPE, "number(1,0)");
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
    public boolean supportsWithClause() {
        return true;
    }

    @Override
    public boolean supportsNonRecursiveWithClause() {
        return true;
    }

    @Override
    public boolean supportsWithClauseHead() {
        return supportsWithClause();
    }

    @Override
    public boolean supportsJoinsInRecursiveCte() {
        return true;
    }

    @Override
    public boolean supportsAnsiRowValueConstructor() {
        return supportsRowValueConstructor();
    }

    @Override
    public boolean supportsRowValueConstructor() {
        return true;
    }

    @Override
    public boolean supportsFullRowValueComparison() {
        return true;
    }

    @Override
    public boolean supportsCountTuple() {
        return false;
    }

    @Override
    public boolean supportsWindowFunctions() {
        return true;
    }

    @Override
    public boolean supportsFilterClause() {
        return false;
    }

    @Override
    public boolean supportsNullPrecedence() {
        return true;
    }

    @Override
    public boolean supportsWindowNullPrecedence() {
        return false;
    }

    @Override
    public boolean supportsBooleanAggregation() {
        return false;
    }

    @Override
    public boolean isNullSmallest() {
        return false;
    }

    @Override
    public boolean requiresNullCast() {
        return true;
    }

    @Override
    public String getPhysicalRowId() {
        return null;
    }

    @Override
    public LateralStyle getLateralStyle() {
        return LateralStyle.LATERAL;
    }

    @Override
    public DeleteJoinStyle getDeleteJoinStyle() {
        return DeleteJoinStyle.NONE;
    }

    @Override
    public UpdateJoinStyle getUpdateJoinStyle() {
        return UpdateJoinStyle.NONE;
    }

    @Override
    public boolean supportsArbitraryLengthMultiset() {
        return false;
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
        return appendExtendedSql(sqlSb, statementType, isSubquery, isEmbedded, withClause, limit, offset, null, returningColumns, includedModificationStates);
    }

    @Override
    public Map<String, String> appendExtendedSql(StringBuilder sqlSb, DbmsStatementType statementType, boolean isSubquery, boolean isEmbedded, StringBuilder withClause, String limit, String offset, String dmlAffectedTable, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
        boolean addParenthesis = isSubquery && sqlSb.length() > 0 && sqlSb.charAt(0) != '(';
        if (addParenthesis) {
            sqlSb.insert(0, '(');
        }

        if (withClause != null) {
            sqlSb.insert(0, withClause);
        }
        if (limit != null) {
            appendLimit(sqlSb, isSubquery, limit, offset);
        }
        if (isSubquery && !supportsModificationQueryInWithClause() && returningColumns != null) {
            throw new IllegalArgumentException("Returning columns in a subquery is not possible for this dbms!");
        }

        if (addParenthesis) {
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
            boolean needsWrapper = hasOuterClause && needsSetOperationWrapper() && operands.size() > 1;

            if (needsWrapper) {
                sqlSb.append("select * from (");
            }

            String[] aliases = appendSetOperands(sqlSb, setType, operator, isSubquery, operands, hasOuterClause);

            if (needsWrapper) {
                closeFromClause(sqlSb);
            }

            appendOrderBy(sqlSb, orderByElements, aliases);

            if (limit != null) {
                appendLimit(sqlSb, isSubquery, limit, offset);
            }
        }

        if (isSubquery) {
            sqlSb.append(')');
        }
    }

    @Override
    public DbmsLimitHandler createLimitHandler() {
        return new DefaultDbmsLimitHandler();
    }

    @Override
    public boolean supportsLimitWithoutOrderBy() {
        return true;
    }

    @Override
    public boolean supportsLimitInQuantifiedPredicateSubquery() {
        return true;
    }

    @Override
    public boolean supportsNestedCorrelations() {
        return true;
    }

    protected String getWindowFunctionDummyOrderBy() {
        return null;
    }

    protected boolean needsAliasInSetOrderBy() {
        return false;
    }

    protected boolean supportsPartitionInRowNumberOver() {
        return false;
    }

    protected boolean needsAliasForFromClause() {
        return false;
    }

    protected boolean needsSetOperationWrapper() {
        return true;
    }

    protected String[] appendSetOperands(StringBuilder sqlSb, SetOperationType setType, String operator, boolean isSubquery, List<String> operands, boolean hasOuterClause) {
        boolean first = true;
        final boolean emulate = setType == SetOperationType.EXCEPT_ALL && !supportsExcept(true) || setType == SetOperationType.INTERSECT_ALL && !supportsIntersect(true);
        final String select = "select ";
        final String windowFunctionDummyOrderBy = getWindowFunctionDummyOrderBy();
        String[] aliases = null;

        if (needsAliasInSetOrderBy()) {
            aliases = SqlUtils.getSelectItemAliases(operands.get(0), SqlUtils.SELECT_FINDER.indexIn(operands.get(0)));
        }

        for (String operand : operands) {
            boolean wasFirst = false;
            if (first) {
                first = false;
                wasFirst = true;
                if (emulate) {
                    if (aliases == null) {
                        aliases = SqlUtils.getSelectItemAliases(operand, SqlUtils.indexOfSelect(operand));
                    }

                    sqlSb.append(select);
                    for (int i = 0; i < aliases.length; i++) {
                        if (i != 0) {
                            sqlSb.append(", ");
                        }
                        sqlSb.append(aliases[i]);
                    }

                    sqlSb.append(" from (");
                }
            } else {
                sqlSb.append(' ');
                sqlSb.append(operator);
                sqlSb.append(' ');
            }

            if (emulate) {
                int selectIndex = SqlUtils.indexOfSelect(operand);
                String[] expressions = SqlUtils.getSelectItemExpressions(operand, selectIndex);

                sqlSb.append(select);
                sqlSb.append("row_number() over (partition by ");

                for (int i = 0; i < expressions.length; i++) {
                    if (i != 0) {
                        sqlSb.append(", ");
                    }
                    sqlSb.append(expressions[i]);
                }

                if (windowFunctionDummyOrderBy != null) {
                    sqlSb.append(windowFunctionDummyOrderBy);
                }
                sqlSb.append(") as set_op_row_num_, ");
                sqlSb.append(operand, select.length(), operand.length());
            } else {
                // Need a wrapper for operands that have an order by clause
                boolean addWrapper = SqlUtils.indexOfOrderBy(operand) != -1;
                if (addWrapper) {
                    sqlSb.append("select * from (");
                }
                if ((addWrapper || wasFirst) && operand.charAt(0) == '(') {
                    sqlSb.append(operand, 1, operand.length() - 1);
                } else {
                    sqlSb.append(operand);
                }
                if (addWrapper) {
                    closeFromClause(sqlSb);
                }
            }
        }

        if (emulate) {
            closeFromClause(sqlSb);
        }

        return aliases;
    }

    private void closeFromClause(StringBuilder sqlSb) {
        sqlSb.append(')');
        if (needsAliasForFromClause()) {
            sqlSb.append(" set_op");
        }
    }

    protected void appendOrderBy(StringBuilder sqlSb, List<? extends OrderByElement> orderByElements, String[] aliases) {
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

            appendOrderByElement(sqlSb, element, aliases);
        }
    }

    @Override
    public void appendOrderByElement(StringBuilder sqlSb, OrderByElement element, String[] aliases) {
        if (aliases != null) {
            sqlSb.append(aliases[element.getPosition() - 1]);
        } else {
            sqlSb.append(element.getPosition());
        }

        if (element.isAscending()) {
            sqlSb.append(" asc");
        } else {
            sqlSb.append(" desc");
        }
        if (element.isNullable()) {
            if (element.isNullsFirst()) {
                sqlSb.append(" nulls first");
            } else {
                sqlSb.append(" nulls last");
            }
        }
    }

    protected void appendEmulatedOrderByElementWithNulls(StringBuilder sqlSb, OrderByElement element, String[] aliases) {
        sqlSb.append("case when ");
        if (aliases != null) {
            sqlSb.append(aliases[element.getPosition() - 1]);
        } else {
            sqlSb.append(element.getPosition());
        }
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
            default: throw new IllegalArgumentException("Unknown operation type: " + type);
        }
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
    public boolean supportsGroupByExpressionInHavingMatching() {
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
    public boolean needsCastParameters() {
        return true;
    }

    @Override
    public String getDummyTable() {
        return null;
    }

    @Override
    public String cast(String expression, String sqlType) {
        return "cast(" + expression + " as " + sqlType + ")";
    }

    @Override
    public boolean needsReturningSqlTypes() {
        return false;
    }

    @Override
    public int getPrepareFlags() {
        return Statement.RETURN_GENERATED_KEYS;
    }

    @Override
    public PreparedStatement prepare(PreparedStatement ps, int[] returningSqlTypes) throws SQLException {
        return ps;
    }

    @Override
    public ResultSet extractReturningResult(PreparedStatement ps) throws SQLException {
        return ps.getGeneratedKeys();
    }

    public void appendLimit(StringBuilder sqlSb, boolean isSubquery, String limit, String offset) {
        createLimitHandler().applySql(sqlSb, isSubquery, limit, offset);
    }
}
