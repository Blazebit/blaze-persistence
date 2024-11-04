/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate.base;

import com.blazebit.persistence.spi.DbmsDialect;
import org.hibernate.sql.exec.spi.ExecutionContext;

/**
 * @author Christian Beikov
 * @since 1.6.7
 */
public interface HibernateAccess {

    public ExecutionContext wrapExecutionContext(ExecutionContext executionContext, DbmsDialect dbmsDialect, String[][] returningColumns, int[] returningColumnTypes, HibernateReturningResult<Object[]> returningResult);
}
