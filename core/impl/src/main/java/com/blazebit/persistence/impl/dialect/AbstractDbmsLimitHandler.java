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

import com.blazebit.persistence.spi.DbmsLimitHandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractDbmsLimitHandler implements DbmsLimitHandler {

    private final int length;

    /**
     * @param length The expected maximum length of the string representation for the LIMIT/OFFSET SQL
     */
    public AbstractDbmsLimitHandler(int length) {
        this.length = length;
    }

    @Override
    public boolean supportsLimit() {
        return true;
    }

    @Override
    public boolean supportsLimitOffset() {
        return true;
    }

    @Override
    public boolean limitIncludesOffset() {
        return false;
    }

    /**
     * Returns whether the dbms supports parameters for LIMIT and OFFSET via prepared statements.
     *
     * @return True if parameters allowed, otherwise false
     */
    protected abstract boolean supportsVariableLimit();

    @Override
    public String applySql(String sql, boolean isSubquery, Integer limit, Integer offset) {
        if (limit == null && offset == null) {
            return sql;
        }

        StringBuilder sb = new StringBuilder(sql.length() + length);
        sb.append(sql);

        if (supportsVariableLimit()) {
            applySql(sb, isSubquery, limit == null ? null : "?", offset == null ? null : "?");
        } else {
            if (limitIncludesOffset() && offset != null && limit != null) {
                applySql(sb, isSubquery, Integer.toString(limit + offset), offset.toString());
            } else {
                applySql(sb, isSubquery, limit == null ? null : limit.toString(), offset == null ? null : offset.toString());
            }
        }

        return sb.toString();
    }

    @Override
    public String applySqlInlined(String sql, boolean isSubquery, Integer limit, Integer offset) {
        if (limit == null && offset == null) {
            return sql;
        }

        StringBuilder sb = new StringBuilder(sql.length() + length);
        sb.append(sql);
        if (limitIncludesOffset() && offset != null && limit != null) {
            applySql(sb, isSubquery, Integer.toString(limit + offset), offset.toString());
        } else {
            applySql(sb, isSubquery, limit == null ? null : limit.toString(), offset == null ? null : offset.toString());
        }
        return sb.toString();
    }

    @Override
    public int bindLimitParametersAtStartOfQuery(Integer limit, Integer offset, PreparedStatement statement, int index) throws SQLException {
        return 0;
    }

    @Override
    public void setMaxRows(Integer limit, Integer offset, PreparedStatement statement) throws SQLException {
    }

}
