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
public class NoopDbmsLimitHandler implements DbmsLimitHandler {

    @Override
    public boolean supportsLimit() {
        return false;
    }

    @Override
    public boolean supportsLimitOffset() {
        return false;
    }

    @Override
    public boolean limitIncludesOffset() {
        return false;
    }

    @Override
    public String applySql(String sql, boolean isSubquery, Integer limit, Integer offset) {
        return sql;
    }

    @Override
    public void applySql(StringBuilder sqlSb, boolean isSubquery, String limit, String offset) {
    }

    @Override
    public String applySqlInlined(String sql, boolean isSubquery, Integer limit, Integer offset) {
        return sql;
    }

    @Override
    public int bindLimitParametersAtStartOfQuery(Integer limit, Integer offset, PreparedStatement statement, int index) throws SQLException {
        return 0;
    }

    @Override
    public int bindLimitParametersAtEndOfQuery(Integer limit, Integer offset, PreparedStatement statement, int index) throws SQLException {
        return 0;
    }

    @Override
    public void setMaxRows(Integer limit, Integer offset, PreparedStatement statement) throws SQLException {
        if (limit != null) {
            if (offset != null) {
                statement.setMaxRows(limit + offset);
            } else {
                statement.setMaxRows(limit);
            }
        }
    }

}
