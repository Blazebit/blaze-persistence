/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.window.oragg;

import com.blazebit.persistence.impl.function.window.AbstractWindowFunction;
import com.blazebit.persistence.spi.DbmsDialect;

/**
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
public class WindowOrAggFunction extends AbstractWindowFunction {

    public static final String FUNCTION_NAME = "WINDOW_OR_AGG";

    public WindowOrAggFunction(DbmsDialect dbmsDialect) {
        super("BOOL_OR", dbmsDialect.isNullSmallest(), dbmsDialect.supportsWindowNullPrecedence(), dbmsDialect.supportsFilterClause(), true);
    }

    public WindowOrAggFunction(String functionName, DbmsDialect dbmsDialect) {
        super(functionName, dbmsDialect.isNullSmallest(), dbmsDialect.supportsWindowNullPrecedence(), dbmsDialect.supportsFilterClause(), true);
    }

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return Boolean.class;
    }

}
