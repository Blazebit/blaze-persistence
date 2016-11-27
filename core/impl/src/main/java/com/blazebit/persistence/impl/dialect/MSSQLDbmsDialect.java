/*
 * Copyright 2014 - 2016 Blazebit.
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

import com.blazebit.persistence.spi.DbmsLimitHandler;
import com.blazebit.persistence.spi.OrderByElement;
import com.blazebit.persistence.spi.SetOperationType;

import java.util.List;

public class MSSQLDbmsDialect extends DefaultDbmsDialect {

    @Override
    public String getWithClause(boolean recursive) {
        return "with";
    }

    @Override
    protected String getWindowFunctionDummyOrderBy() {
        return " order by (select 0)";
    }

    @Override
    public boolean supportsReturningGeneratedKeys() {
        // TODO: Implement support for returning
        // https://msdn.microsoft.com/en-us/library/ms177564(v=sql.105).aspx
        return false;
    }

    @Override
    public boolean supportsReturningAllGeneratedKeys() {
        // TODO: Implement support for returning
        // https://msdn.microsoft.com/en-us/library/ms177564(v=sql.105).aspx
        return false;
    }

    @Override
    public boolean supportsReturningColumns() {
        // TODO: Implement support for returning
        // https://msdn.microsoft.com/en-us/library/ms177564(v=sql.105).aspx
        return false;
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
            case EXCEPT: return "MINUS";
            case EXCEPT_ALL: return "MINUS";
            default: throw new IllegalArgumentException("Unknown operation type: " + type);
        }
    }

    @Override
    public void appendSet(StringBuilder sqlSb, SetOperationType setType, boolean isSubquery, List<String> operands, List<? extends OrderByElement> orderByElements, String limit, String offset) {
        // TODO: Implement all emulation: http://www.sqlpassion.at/archive/2015/02/16/intersect-sql-server-2/

        // TODO: Need to wrap set operations and alias non-simple expressions from order by: https://msdn.microsoft.com/en-us/library/ms188385.aspx#Anchor_4
        super.appendSet(sqlSb, setType, isSubquery, operands, orderByElements, limit, offset);
    }

    @Override
    protected void appendSetOperands(StringBuilder sqlSb, SetOperationType setType, String operator, boolean isSubquery, List<String> operands, boolean hasOuterClause) {
        if (!hasOuterClause) {
            super.appendSetOperands(sqlSb, setType, operator, isSubquery, operands, hasOuterClause);
        } else {
            sqlSb.append("select * from (");
            super.appendSetOperands(sqlSb, setType, operator, isSubquery, operands, hasOuterClause);
            sqlSb.append(')');
        }
    }

    @Override
    protected void appendOrderByElement(StringBuilder sqlSb, OrderByElement element) {
        if ((element.isAscending() && element.isNullsFirst()) || (!element.isAscending() && !element.isNullsFirst())) {
            // The following are the defaults, so just let them through
            // ASC + NULLS FIRST
            // DESC + NULLS LAST
            sqlSb.append(element.getPosition());

            if (element.isAscending()) {
                sqlSb.append(" asc");
            } else {
                sqlSb.append(" desc");
            }
        } else {
            appendEmulatedOrderByElementWithNulls(sqlSb, element);
        }
    }

    @Override
    public DbmsLimitHandler createLimitHandler() {
        return new MSSQL2012DbmsLimitHandler();
    }
}
