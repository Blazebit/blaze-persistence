package com.blazebit.persistence.impl.hibernate;

import com.blazebit.persistence.spi.DbmsDialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.pagination.LimitHandler;

public class Hibernate60LimitHandlingDialect extends Hibernate60DelegatingDialect {

    private final DbmsDialect dbmsDialect;

    public Hibernate60LimitHandlingDialect(Dialect delegate, DbmsDialect dbmsDialect) {
        super(delegate);
        this.dbmsDialect = dbmsDialect;
    }

    @Override
    public LimitHandler getLimitHandler() {
        return new Hibernate60LimitHandler(this, dbmsDialect);
    }

}
