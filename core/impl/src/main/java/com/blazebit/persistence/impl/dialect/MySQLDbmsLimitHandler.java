/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.dialect;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MySQLDbmsLimitHandler extends AbstractDbmsLimitHandler {

    public MySQLDbmsLimitHandler() {
        super(20);
    }

    public MySQLDbmsLimitHandler(int length) {
        super(length);
    }

    @Override
    public boolean supportsVariableLimit() {
        return true;
    }

    @Override
    public void applySql(StringBuilder sqlSb, boolean isSubquery, String limit, String offset) {
        if (limit != null) {
            if (offset != null) {
                sqlSb.append(" limit ").append(offset).append(',').append(limit);
            } else {
                sqlSb.append(" limit ").append(limit);
            }
        } else if (offset != null) {
            // The biggest possible limit to be able to make an OFFSET only query
            sqlSb.append(" limit ").append(offset).append(",18446744073709551610");
        }
    }

    @Override
    public int bindLimitParametersAtEndOfQuery(Integer limit, Integer offset, PreparedStatement statement, int index) throws SQLException {
        if (limit != null) {
            if (offset != null) {
                statement.setInt(index, offset);
                statement.setInt(index + 1, limit);
                return 2;
            } else {
                statement.setInt(index, limit);
                return 1;
            }
        } else if (offset != null) {
            statement.setInt(index, offset);
            return 1;
        }

        return 0;
    }

}
