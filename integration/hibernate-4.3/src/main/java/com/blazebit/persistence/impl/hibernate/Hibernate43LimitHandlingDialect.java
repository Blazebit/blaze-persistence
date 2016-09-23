package com.blazebit.persistence.impl.hibernate;

import com.blazebit.persistence.spi.DbmsDialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.engine.spi.RowSelection;

public class Hibernate43LimitHandlingDialect extends Hibernate43DelegatingDialect {

    private final DbmsDialect dbmsDialect;

    public Hibernate43LimitHandlingDialect(Dialect delegate, DbmsDialect dbmsDialect) {
        super(delegate);
        this.dbmsDialect = dbmsDialect;
    }

    @Override
    public LimitHandler buildLimitHandler(String sql, RowSelection selection) {
        return new Hibernate43LimitHandler(this, dbmsDialect, sql, selection);
    }
}
