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

import com.blazebit.persistence.impl.util.BoyerMooreCaseInsensitiveAsciiLastPatternFinder;
import com.blazebit.persistence.impl.util.PatternFinder;
import com.blazebit.persistence.impl.util.SqlUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class OracleDbmsLimitHandler extends AbstractDbmsLimitHandler {

    private static final PatternFinder FOR_UPDATE_FINDER = new BoyerMooreCaseInsensitiveAsciiLastPatternFinder("for update");

    public OracleDbmsLimitHandler() {
        super(40);
    }

    public OracleDbmsLimitHandler(int length) {
        super(length);
    }

    @Override
    public boolean supportsVariableLimit() {
        return true;
    }

    @Override
    public boolean limitIncludesOffset() {
        return true;
    }

    @Override
    public void applySql(StringBuilder sqlSb, boolean isSubquery, String limit, String offset) {
        int selectIndex = SqlUtils.indexOfSelect(sqlSb);

        if (offset != null) {
            if (limit != null) {
                // Need to extract aliases and place them in the outer select so we don't screw up subqueries
                String[] aliases = SqlUtils.getSelectItemAliases(sqlSb, selectIndex);
                StringBuilder selectClauseSb = new StringBuilder(60 + aliases.length * 20);
                selectClauseSb.append("select ");

                for (int i = 0; i < aliases.length; i++) {
                    selectClauseSb.append(aliases[i]);
                    selectClauseSb.append(',');
                }
                selectClauseSb.setCharAt(selectClauseSb.length() - 1, ' ');
                selectClauseSb.append("from ( select row_.*, rownum rownum_ from ( ");

                // We know we will need some more
                sqlSb.ensureCapacity(sqlSb.length() + 50 + selectClauseSb.length());
                sqlSb.insert(selectIndex, selectClauseSb);
            } else {
                sqlSb.ensureCapacity(sqlSb.length() + 50);
                sqlSb.insert(selectIndex, "select * from ( ");
            }
        } else {
            sqlSb.ensureCapacity(sqlSb.length() + 50);
            sqlSb.insert(selectIndex, "select * from ( ");
        }

        // The FOR UPDATE clause must be the last, so we find the index at which we should insert
        int forUpdateIndex = FOR_UPDATE_FINDER.indexIn(sqlSb);
        forUpdateIndex = forUpdateIndex == -1 ? sqlSb.length() : forUpdateIndex - 1;

        if (offset != null) {
            if (limit != null) {
                sqlSb.insert(forUpdateIndex, " ) row_ where rownum <= " + limit + ") where rownum_ > " + offset);
            } else {
                sqlSb.insert(forUpdateIndex, " ) where rownum > " + offset);
            }
        } else {
            sqlSb.insert(forUpdateIndex, " ) where rownum <= " + limit);
        }
    }

    @Override
    public int bindLimitParametersAtEndOfQuery(Integer limit, Integer offset, PreparedStatement statement, int index) throws SQLException {
        if (offset != null) {
            if (limit != null) {
                statement.setInt(index, limit + offset);
                statement.setInt(index + 1, offset);
                return 2;
            } else {
                statement.setInt(index, offset);
                return 1;
            }
        } else if (limit != null) {
            statement.setInt(index, limit);
            return 1;
        }

        return 0;
    }

}
