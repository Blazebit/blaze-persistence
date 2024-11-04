/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.window.percentilecont;

import com.blazebit.persistence.impl.function.window.AbstractWindowFunction;
import com.blazebit.persistence.spi.DbmsDialect;

/**
 * The percentile_cont ordered set aggregate function.
 *
 * @author Christian Beikov
 * @since 1.6.4
 */
public class PercentileContFunction extends AbstractWindowFunction {

    public static final String FUNCTION_NAME = "PERCENTILE_CONT";

    public PercentileContFunction(DbmsDialect dbmsDialect) {
        super(FUNCTION_NAME, dbmsDialect.isNullSmallest(), dbmsDialect.supportsWindowNullPrecedence(), dbmsDialect.supportsFilterClause(), true);
    }

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return Double.class;
    }

}
