/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.persistence.spi.DbmsDialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.pagination.LimitHandler;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class Hibernate5LimitHandlingDialect extends Hibernate5DelegatingDialect {

    private final DbmsDialect dbmsDialect;

    public Hibernate5LimitHandlingDialect(Dialect delegate, DbmsDialect dbmsDialect) {
        super(delegate);
        this.dbmsDialect = dbmsDialect;
    }

    @Override
    public LimitHandler getLimitHandler() {
        return new Hibernate5LimitHandler(this, dbmsDialect);
    }

}
