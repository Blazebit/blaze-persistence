package com.blazebit.persistence.impl.dialect;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DefaultDbmsLimitHandler extends AbstractDbmsLimitHandler {

    public DefaultDbmsLimitHandler() {
        super(20);
    }

    @Override
    public boolean supportsVariableLimit() {
        return false;
    }

    @Override
    public void applySql(StringBuilder sqlSb, boolean isSubquery, String limit, String offset) {
        if (limit != null) {
            sqlSb.append(" limit ").append(limit);
        }
        if (offset != null) {
            sqlSb.append(" offset ").append(offset);
        }
    }

    @Override
    public int bindLimitParametersAtEndOfQuery(Integer limit, Integer offset, PreparedStatement statement, int index) throws SQLException {
        if (limit != null) {
            statement.setInt(index, limit);
            if (offset != null) {
                statement.setInt(index + 1, offset);
                return 2;
            } else {
                return 1;
            }
        } else if (offset != null) {
            statement.setInt(index, offset);
            return 1;
        }

        return 0;
    }

}
