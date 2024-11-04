/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.testsuite.base.jpa;

import java.sql.SQLException;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class UncheckedSqlException extends RuntimeException {

    public UncheckedSqlException(SQLException sqlException) {
        super(sqlException);
    }
}
