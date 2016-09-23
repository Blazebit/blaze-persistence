package com.blazebit.persistence.impl.dialect;

import com.blazebit.persistence.spi.DbmsLimitHandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;

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
    public boolean supportsVariableLimit() {
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
