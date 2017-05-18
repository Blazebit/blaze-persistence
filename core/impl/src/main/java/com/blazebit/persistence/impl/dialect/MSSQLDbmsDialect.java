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

import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.DbmsLimitHandler;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.OrderByElement;
import com.blazebit.persistence.spi.SetOperationType;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public class MSSQLDbmsDialect extends DefaultDbmsDialect {

    public MSSQLDbmsDialect() {
    }

    public MSSQLDbmsDialect(Map<Class<?>, String> childSqlTypes) {
        super(childSqlTypes);
    }

    @Override
    public String getWithClause(boolean recursive) {
        return "with";
    }

    @Override
    protected String getWindowFunctionDummyOrderBy() {
        return " order by (select 0)";
    }

    @Override
    public boolean supportsReturningColumns() {
        return true;
    }

    @Override
    public boolean supportsComplexGroupBy() {
        // SQL Server bug? https://support.microsoft.com/en-us/kb/2873474
        return false;
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
            case EXCEPT: return "EXCEPT";
            case EXCEPT_ALL: return "EXCEPT";
            default: throw new IllegalArgumentException("Unknown operation type: " + type);
        }
    }

    @Override
    protected boolean supportsPartitionInRowNumberOver() {
        return true;
    }

    @Override
    public boolean supportsFullRowValueComparison() {
        return false;
    }

    @Override
    public Map<String, String> appendExtendedSql(StringBuilder sqlSb, DbmsStatementType statementType, boolean isSubquery, boolean isEmbedded, StringBuilder withClause, String limit, String offset, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
        boolean addParenthesis = isSubquery && sqlSb.length() > 0 && sqlSb.charAt(0) != '(';
        if (addParenthesis) {
            sqlSb.insert(0, '(');
        }

        if (withClause != null) {
            sqlSb.insert(0, withClause);
        }

        if (returningColumns != null) {
            if (isSubquery) {
                throw new IllegalArgumentException("Returning columns in a subquery is not possible for this dbms!");
            }

            StringBuilder outputSb = new StringBuilder();
            outputSb.append(" output ");
            for (int i = 0; i < returningColumns.length; i++) {
                if (i != 0) {
                    outputSb.append(',');
                }
                if (statementType == DbmsStatementType.DELETE) {
                    outputSb.append("deleted.");
                } else {
                    outputSb.append("inserted.");
                }
                outputSb.append(returningColumns[i]);
            }

            if (statementType == DbmsStatementType.DELETE || statementType == DbmsStatementType.UPDATE) {
                int whereIndex = SqlUtils.indexOfWhere(sqlSb);
                if (whereIndex == -1) {
                    sqlSb.append(outputSb);
                } else {
                    sqlSb.insert(whereIndex, outputSb);
                }
            } else if (statementType == DbmsStatementType.INSERT) {
                int selectIndex = SqlUtils.indexOfSelect(sqlSb);
                sqlSb.insert(selectIndex - 1, outputSb);
            }
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
    protected boolean needsAliasForFromClause() {
        return true;
    }

    @Override
    protected boolean needsAliasInSetOrderBy() {
        return true;
    }

    @Override
    protected void appendOrderByElement(StringBuilder sqlSb, OrderByElement element, String[] aliases) {
        if (!element.isNullable() || (element.isAscending() && element.isNullsFirst()) || (!element.isAscending() && !element.isNullsFirst())) {
            // The following are the defaults, so just let them through
            // ASC + NULLS FIRST
            // DESC + NULLS LAST
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
        } else {
            appendEmulatedOrderByElementWithNulls(sqlSb, element, aliases);
        }
    }

    @Override
    public DbmsLimitHandler createLimitHandler() {
        return new MSSQL2012DbmsLimitHandler();
    }
}
