package com.blazebit.persistence.impl.dialect;

import com.blazebit.persistence.spi.DbmsLimitHandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQL2008DbmsLimitHandler extends AbstractDbmsLimitHandler {

    public SQL2008DbmsLimitHandler() {
        super(40);
    }

    @Override
    public boolean supportsVariableLimit() {
        return true;
    }

    @Override
    public void applySql(StringBuilder sqlSb, boolean isSubquery, String limit, String offset) {
        if (offset != null) {
            sqlSb.append(" offset ").append(offset).append(" rows");
            if (limit != null) {
                sqlSb.append(" fetch next ").append(limit).append(" rows only");
            }
        } else if (limit != null) {
            sqlSb.append(" fetch first ").append(limit).append(" rows only");
        }
    }

    @Override
    public int bindLimitParametersAtEndOfQuery(Integer limit, Integer offset, PreparedStatement statement, int index) throws SQLException {
        if (offset != null) {
            statement.setInt(index, offset);

            if (limit != null) {
                statement.setInt(index + 1, limit);
                return 2;
            }
            return 1;
        } if (limit != null) {
            statement.setInt(index, limit);
            return 1;
        }

        return 0;
    }

}
