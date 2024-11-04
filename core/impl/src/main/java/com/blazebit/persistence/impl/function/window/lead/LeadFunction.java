/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.window.lead;

import com.blazebit.persistence.impl.function.window.AbstractWindowFunction;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
public class LeadFunction extends AbstractWindowFunction {

    public static final String FUNCTION_NAME = "LEAD";

    public LeadFunction(DbmsDialect dbmsDialect) {
        super(FUNCTION_NAME, dbmsDialect.isNullSmallest(), dbmsDialect.supportsWindowNullPrecedence(), false, true);
    }

    @Override
    protected void renderArgument(FunctionRenderContext context, WindowFunction windowFunction, String caseWhenPre, String caseWhenPost, String argument, int argumentIndex) {
        // Only the second argument will not receive the CASE WHEN wrapper
        if (caseWhenPre == null || argumentIndex == 1) {
            context.addChunk(argument);
        } else {
            context.addChunk(caseWhenPre);
            context.addChunk(argument);
            context.addChunk(caseWhenPost);
        }
    }

    @Override
    protected boolean requiresOver() {
        return true;
    }
}
