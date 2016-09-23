package com.blazebit.persistence.impl.hibernate;

import com.blazebit.persistence.spi.DbmsDialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.pagination.LimitHandler;

public class Hibernate52LimitHandlingDialect extends Hibernate52DelegatingDialect {

    private final DbmsDialect dbmsDialect;

    public Hibernate52LimitHandlingDialect(Dialect delegate, DbmsDialect dbmsDialect) {
        super(delegate);
        this.dbmsDialect = dbmsDialect;
    }

    @Override
    public LimitHandler getLimitHandler() {
        return new Hibernate52LimitHandler(this, dbmsDialect);
    }

}
