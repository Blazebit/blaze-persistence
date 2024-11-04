/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.window.groupconcat;

import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class DB2GroupConcatWindowFunction extends AbstractGroupConcatWindowFunction {

    public DB2GroupConcatWindowFunction(DbmsDialect dbmsDialect) {
        super("listagg", dbmsDialect.isNullSmallest(), false, dbmsDialect.supportsFilterClause(), true);
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
}
