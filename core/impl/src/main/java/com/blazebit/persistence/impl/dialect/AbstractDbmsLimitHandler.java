package com.blazebit.persistence.impl.dialect;

import com.blazebit.persistence.spi.DbmsLimitHandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class AbstractDbmsLimitHandler implements DbmsLimitHandler {

    private final int length;

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
