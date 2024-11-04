/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.window.percentrank;

import com.blazebit.persistence.impl.function.window.AbstractWindowFunction;
import com.blazebit.persistence.spi.DbmsDialect;

/**
 * Relative rank of the current row: (rank - 1) / (total rows - 1)
 *
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
public class PercentRankFunction extends AbstractWindowFunction {

    public static final String FUNCTION_NAME = "PERCENT_RANK";

    public PercentRankFunction(DbmsDialect dbmsDialect) {
        super(FUNCTION_NAME, dbmsDialect.isNullSmallest(), dbmsDialect.supportsWindowNullPrecedence(), dbmsDialect.supportsFilterClause(), false);
    }

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return Double.class;
    }

    @Override
    protected boolean requiresOver() {
        return true;
    }
}
