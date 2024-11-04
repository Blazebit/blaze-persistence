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
public class MySQLGroupConcatWindowFunction extends AbstractGroupConcatWindowFunction {

    public MySQLGroupConcatWindowFunction(DbmsDialect dbmsDialect) {
        super("group_concat", dbmsDialect.isNullSmallest(), false, dbmsDialect.supportsFilterClause(), true);
    }

    protected void renderArguments(FunctionRenderContext context, WindowFunction windowFunction) {
        GroupConcat groupConcat = (GroupConcat) windowFunction;
        if (groupConcat.isDistinct()) {
            context.addChunk("distinct ");
        }
        super.renderArguments(context, windowFunction);
        context.addChunk(" ");
        super.renderOrderBy(context, windowFunction.getWithinGroup());
        context.addChunk(" separator ");
        context.addChunk(quoted(groupConcat.getSeparator()));
    }

    @Override
    protected void renderWithinGroup(FunctionRenderContext context, List<Order> orderBys) {
        // Don't render the WITHIN GROUP clause as it is inlined into the function arguments
    }
}
