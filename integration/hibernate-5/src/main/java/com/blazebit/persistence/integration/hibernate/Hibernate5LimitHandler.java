/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.DbmsLimitHandler;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.engine.spi.RowSelection;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class Hibernate5LimitHandler implements LimitHandler {

    private final DbmsLimitHandler limitHandler;
    private Integer limit;
    private Integer offset;

    public Hibernate5LimitHandler(Dialect dialect, DbmsDialect dbmsDialect) {
        this.limitHandler = dbmsDialect.createLimitHandler();
    }

    @Override
    public boolean supportsLimit() {
        return limitHandler.supportsLimit();
    }

    @Override
    public boolean supportsLimitOffset() {
        return limitHandler.supportsLimitOffset();
    }

    @Override
    public String processSql(String sql, RowSelection selection) {
        if (selection == null || selection.getMaxRows() == null || selection.getMaxRows().intValue() == Integer.MAX_VALUE) {
            this.limit = null;
        } else {
            this.limit = selection.getMaxRows();
        }
        if (selection == null || selection.getFirstRow() == null || selection.getFirstRow().intValue() < 1) {
            this.offset = null;
        } else {
            this.offset = selection.getFirstRow();
        }
        return limitHandler.applySql(sql, false, limit, offset);
    }

    @Override
    public int bindLimitParametersAtStartOfQuery(RowSelection selection, PreparedStatement statement, int index) throws SQLException {
        return limitHandler.bindLimitParametersAtStartOfQuery(limit, offset, statement, index);
    }

    @Override
    public int bindLimitParametersAtEndOfQuery(RowSelection selection, PreparedStatement statement, int index) throws SQLException {
        return limitHandler.bindLimitParametersAtEndOfQuery(limit, offset, statement, index);
    }

    @Override
    public void setMaxRows(RowSelection selection, PreparedStatement statement) throws SQLException {
        limitHandler.setMaxRows(limit, offset, statement);
    }
}
