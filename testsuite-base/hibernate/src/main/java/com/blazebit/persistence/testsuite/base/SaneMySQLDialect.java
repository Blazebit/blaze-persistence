/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.base;

import org.hibernate.dialect.MySQL5InnoDBDialect;

import java.sql.Types;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class SaneMySQLDialect extends MySQL5InnoDBDialect {

    public SaneMySQLDialect() {
        registerColumnType( Types.TIME, "time(6)" );
        registerColumnType( Types.TIMESTAMP, "datetime(6)" );
    }

    @Override
    public String getTableTypeString() {
        return " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_bin";
    }
}
