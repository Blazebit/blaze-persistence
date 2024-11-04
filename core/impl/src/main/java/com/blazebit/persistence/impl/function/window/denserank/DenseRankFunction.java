/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.window.denserank;

import com.blazebit.persistence.impl.function.window.AbstractWindowFunction;
import com.blazebit.persistence.spi.DbmsDialect;

/**
 * Rank of the current row without gaps; this function counts peer groups
 *
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
public class DenseRankFunction extends AbstractWindowFunction {

    public static final String FUNCTION_NAME = "DENSE_RANK";

    public DenseRankFunction(DbmsDialect dbmsDialect) {
        super(FUNCTION_NAME, dbmsDialect.isNullSmallest(), dbmsDialect.supportsWindowNullPrecedence(), dbmsDialect.supportsFilterClause(), false);
    }

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return Integer.class;
    }

    @Override
    protected boolean requiresOver() {
        return true;
    }
}
