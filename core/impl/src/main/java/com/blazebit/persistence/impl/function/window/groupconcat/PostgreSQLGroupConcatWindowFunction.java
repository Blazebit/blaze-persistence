/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.window.groupconcat;

import com.blazebit.persistence.impl.function.Order;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.FunctionRenderContext;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class PostgreSQLGroupConcatWindowFunction extends AbstractGroupConcatWindowFunction {

    public PostgreSQLGroupConcatWindowFunction(DbmsDialect dbmsDialect) {
        super("string_agg", dbmsDialect.isNullSmallest(), true, dbmsDialect.supportsFilterClause(), true);
    }

    protected void renderArguments(FunctionRenderContext context, WindowFunction windowFunction) {
        GroupConcat groupConcat = (GroupConcat) windowFunction;
        if (groupConcat.isDistinct()) {
            context.addChunk("distinct ");
        }
        super.renderArguments(context, windowFunction);
        context.addChunk(", ");
        context.addChunk(quoted(groupConcat.getSeparator()));
        context.addChunk(" ");
        super.renderOrderBy(context, windowFunction.getWithinGroup());
    }

    @Override
    protected void renderWithinGroup(FunctionRenderContext context, List<Order> orderBys) {
        // Don't render the WITHIN GROUP clause as it is inlined into the function arguments
    }
}
