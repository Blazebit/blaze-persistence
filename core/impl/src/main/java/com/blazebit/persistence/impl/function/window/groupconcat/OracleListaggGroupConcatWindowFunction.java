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
public class OracleListaggGroupConcatWindowFunction extends AbstractGroupConcatWindowFunction {

    public OracleListaggGroupConcatWindowFunction(DbmsDialect dbmsDialect) {
        super("listagg", dbmsDialect.isNullSmallest(), dbmsDialect.supportsWindowNullPrecedence(), dbmsDialect.supportsFilterClause(), true);
    }

    protected void renderArguments(FunctionRenderContext context, WindowFunction windowFunction) {
        GroupConcat groupConcat = (GroupConcat) windowFunction;
        if (groupConcat.isDistinct()) {
            context.addChunk("distinct ");
        }
        super.renderArguments(context, windowFunction);
        context.addChunk(", ");
        context.addChunk(quoted(groupConcat.getSeparator()));
    }

    @Override
    protected void renderWithinGroup(FunctionRenderContext context, List<Order> orderBys) {
        if (orderBys.size() == 0) {
            context.addChunk(" within group (1)");
        } else {
            super.renderWithinGroup(context, orderBys);
        }
    }
}
