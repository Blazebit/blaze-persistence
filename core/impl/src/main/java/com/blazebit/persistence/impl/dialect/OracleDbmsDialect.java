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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.DbmsLimitHandler;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.DeleteJoinStyle;
import com.blazebit.persistence.spi.SetOperationType;
import com.blazebit.persistence.spi.UpdateJoinStyle;
import com.blazebit.persistence.spi.ValuesStrategy;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public class OracleDbmsDialect extends DefaultDbmsDialect {

    private static final Class<?> ORACLE_PREPARED_STATEMENT_CLASS;
    private static final Method REGISTER_RETURN_PARAMETER;
    private static final Method GET_RETURN_RESULT_SET;

    static {
        Class<?> clazz = null;
        Method registerReturnParameterMethod = null;
        Method getReturnResultSetMethod = null;
        try {
            clazz = Class.forName("oracle.jdbc.OraclePreparedStatement");
            registerReturnParameterMethod = clazz.getMethod("registerReturnParameter", int.class, int.class);
            getReturnResultSetMethod = clazz.getMethod("getReturnResultSet");
        } catch (Exception e) {
            // Ignore
        }

        ORACLE_PREPARED_STATEMENT_CLASS = clazz;
        REGISTER_RETURN_PARAMETER = registerReturnParameterMethod;
        GET_RETURN_RESULT_SET = getReturnResultSetMethod;
    }

    public OracleDbmsDialect() {
        super(getSqlTypes());
    }

    public OracleDbmsDialect(Map<Class<?>, String> childSqlTypes) {
        super(childSqlTypes);
    }

    protected static Map<Class<?>, String> getSqlTypes() {
        Map<Class<?>, String> types = new HashMap<Class<?>, String>();

        types.put(Boolean.class, "boolean");
        types.put(Boolean.TYPE, "boolean");
        types.put(Byte.class, "number(3,0)");
        types.put(Byte.TYPE, "number(3,0)");
        types.put(Short.class, "number(5,0)");
        types.put(Short.TYPE, "number(5,0)");
        types.put(Integer.class, "number(10,0)");
        types.put(Integer.TYPE, "number(10,0)");
        types.put(Long.class, "number(19,0)");
        types.put(Long.TYPE, "number(19,0)");

        types.put(Character.class, "char(1)");
        types.put(Character.TYPE, "char(1)");

        types.put(String.class, "clob");
        types.put(BigInteger.class, "number");
        types.put(BigDecimal.class, "decimal");

        types.put(Time.class, "date");
        types.put(java.sql.Date.class, "date");
        types.put(Timestamp.class, "date");
        types.put(java.util.Date.class, "date");
        types.put(java.util.Calendar.class, "date");

        return types;
    }

    @Override
    public String cast(String expression, String sqlType) {
        if ("clob".equals(sqlType)) {
            return "to_clob(" + expression + ")";
        }
        return super.cast(expression, sqlType);
    }

    @Override
    public String getWithClause(boolean recursive) {
        // NOTE: For 10g return !recursive
        return "with";
    }

    @Override
    public boolean supportsWithClauseHead() {
        // NOTE: For 10g return false
        return true;
    }

    @Override
    protected String getOperator(SetOperationType type) {
        if (type == null) {
            return null;
        }

        switch (type) {
            case UNION: return "UNION";
            case UNION_ALL: return "UNION ALL";
            case INTERSECT: return "INTERSECT";
            case INTERSECT_ALL: return "INTERSECT";
            case EXCEPT: return "MINUS";
            case EXCEPT_ALL: return "MINUS";
            default: throw new IllegalArgumentException("Unknown operation type: " + type);
        }
    }

    @Override
    protected boolean supportsPartitionInRowNumberOver() {
        return true;
    }

    @Override
    public boolean supportsWindowNullPrecedence() {
        return true;
    }

    @Override
    public String getPhysicalRowId() {
        return "ROWID";
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
    public Map<String, String> appendExtendedSql(StringBuilder sqlSb, DbmsStatementType statementType, boolean isSubquery, boolean isEmbedded, StringBuilder withClause, String limit, String offset, String dmlAffectedTable, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
        boolean addParenthesis = isSubquery && sqlSb.length() > 0 && sqlSb.charAt(0) != '(';
        if (addParenthesis) {
            sqlSb.insert(0, '(');
        }

        if (withClause != null) {
            // NOTE: for delete and update statement we will wrap the WHERE clause and all the others in a synthetic exists query
            if (statementType == DbmsStatementType.DELETE || statementType == DbmsStatementType.UPDATE) {
                int whereIndex = SqlUtils.indexOfWhere(sqlSb);
                if (whereIndex == -1) {
                    throw new IllegalArgumentException("Couldn't find WHERE clause is query for inserting CTE: " + sqlSb);
                }

                String wrappingStart = " where exists(";
                String wrappingSeparator = "select 1 from dual";
                StringBuilder newSb = new StringBuilder(wrappingStart.length() + wrappingSeparator.length() + withClause.length());
                newSb.append(wrappingStart);
                newSb.append(withClause);
                newSb.append(wrappingSeparator);
                sqlSb.insert(whereIndex, newSb);
                sqlSb.append(')');
            } else {
                sqlSb.insert(SqlUtils.SELECT_FINDER.indexIn(sqlSb, 0, sqlSb.length()), withClause);
            }
        }
        if (limit != null) {
            appendLimit(sqlSb, isSubquery, limit, offset);
        }

        if (returningColumns != null) {
            sqlSb.append(" returning ");
            for (int i = 0; i < returningColumns.length; i++) {
                if (i != 0) {
                    sqlSb.append(',');
                }
                sqlSb.append(returningColumns[i]);
            }
            sqlSb.append(" into ");
            for (int i = 0; i < returningColumns.length; i++) {
                if (i != 0) {
                    sqlSb.append(',');
                }
                sqlSb.append('?');
            }
        }
        
        if (addParenthesis) {
            sqlSb.append(')');
        }
        
        return null;
    }

    @Override
    public boolean supportsReturningColumns() {
        return true;
    }

    @Override
    public boolean supportsRowValueConstructor() {
        return false;
    }

    @Override
    public boolean supportsFullRowValueComparison() {
        return false;
    }

    @Override
    public DbmsLimitHandler createLimitHandler() {
        // NOTE: Oracle12c should use SQL2008DbmsLimitHandler
        return new OracleDbmsLimitHandler();
    }

    @Override
    public ValuesStrategy getValuesStrategy() {
        return ValuesStrategy.SELECT_UNION;
    }

    @Override
    public String getDummyTable() {
        return "dual";
    }

    @Override
    protected String getWindowFunctionDummyOrderBy() {
        return " order by (select 0 from dual)";
    }

    @Override
    public boolean needsReturningSqlTypes() {
        return true;
    }

    @Override
    public boolean supportsArbitraryLengthMultiset() {
        return true;
    }

    @Override
    public PreparedStatement prepare(PreparedStatement ps, int[] returningSqlTypes) throws SQLException {
        if (REGISTER_RETURN_PARAMETER == null) {
            throw new IllegalStateException("Could not apply query returning because the class oracle.jdbc.OraclePreparedStatement could not be loaded!");
        }

        try {
            Object realPs = ps.unwrap(ORACLE_PREPARED_STATEMENT_CLASS);
            int offset = (ps.getParameterMetaData().getParameterCount() - returningSqlTypes.length) + 1;
            for (int i = 0; i < returningSqlTypes.length; i++) {
                REGISTER_RETURN_PARAMETER.invoke(realPs, offset + i, returningSqlTypes[i]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ps;
    }

    @Override
    public ResultSet extractReturningResult(PreparedStatement ps) throws SQLException {
        try {
            return (ResultSet) GET_RETURN_RESULT_SET.invoke(ps.unwrap(ORACLE_PREPARED_STATEMENT_CLASS));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
