/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.dialect;

import com.blazebit.persistence.spi.OrderByElement;

import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.6.0
 */
public class CockroachSQLDbmsDialect extends PostgreSQLDbmsDialect {

    public CockroachSQLDbmsDialect() {
    }

    public CockroachSQLDbmsDialect(Map<Class<?>, String> childSqlTypes) {
        super(childSqlTypes);
    }

    @Override
    public void appendOrderByElement(StringBuilder sqlSb, OrderByElement element, String[] aliases) {
        if (!element.isNullable()) {
            super.appendOrderByElement(sqlSb, element, aliases);
        } else {
            appendEmulatedOrderByElementWithNulls(sqlSb, element, aliases);
        }
    }
}
