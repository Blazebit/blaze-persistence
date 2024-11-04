/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.window.row;

import com.blazebit.persistence.impl.function.window.AbstractWindowFunction;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * Number of the current row within its partition, counting from 1.
 *
 * Syntax: row_number()
 *
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
public class RowNumberFunction extends AbstractWindowFunction {

    public static final String FUNCTION_NAME = "ROW_NUMBER";

    public RowNumberFunction(DbmsDialect dbmsDialect) {
        super(FUNCTION_NAME, dbmsDialect.isNullSmallest(), dbmsDialect.supportsWindowNullPrecedence(), dbmsDialect.supportsFilterClause(), false);
    }

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return Integer.class;
    }

    @Override
    protected void renderArguments(FunctionRenderContext context, WindowFunction windowFunction) {
        // No-op
    }

    @Override
    protected boolean requiresOver() {
        return true;
    }
}
