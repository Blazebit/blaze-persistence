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
    public void appendSet(StringBuilder sqlSb, SetOperationType setType, boolean isSubquery, List<String> operands, List<? extends OrderByElement> orderByElements, String limit, String offset) {
        // TODO: Implement all emulation: http://www.sqlpassion.at/archive/2015/02/16/intersect-sql-server-2/

        // TODO: Need to wrap set operations and alias non-simple expressions from order by: https://msdn.microsoft.com/en-us/library/ms188385.aspx#Anchor_4
        super.appendSet(sqlSb, setType, isSubquery, operands, orderByElements, limit, offset);
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
