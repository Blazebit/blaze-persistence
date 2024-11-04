/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.base;

import java.sql.Types;

import org.hibernate.dialect.Oracle10gDialect;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class SaneOracleDialect extends Oracle10gDialect {

    public SaneOracleDialect() {
        registerColumnType( Types.TIME, "timestamp" );
    }
}
