/*
 * Copyright 2014 - 2021 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
public class H2GroupConcatWindowFunction extends AbstractGroupConcatWindowFunction {

    public H2GroupConcatWindowFunction(DbmsDialect dbmsDialect) {
        // In group_concat H2 does not support the nulls clause
        super("group_concat", dbmsDialect.isNullSmallest(), false, dbmsDialect.supportsFilterClause(), true);
    }

    protected void renderArguments(FunctionRenderContext context, WindowFunction windowFunction) {
        GroupConcat groupConcat = (GroupConcat) windowFunction;
        if ((groupConcat).isDistinct()) {
            context.addChunk("distinct ");
        }
        super.renderArguments(context, windowFunction);
        context.addChunk(" separator ");
        context.addChunk(quoted(groupConcat.getSeparator()));
        super.renderOrderBy(context, windowFunction.getOrderBys());
    }

    protected void renderOrderBy(FunctionRenderContext context, List<Order> orderBys) {
        // Don't render the ORDER BY clause in the OVER clause
    }

    @Override
    protected boolean optimizeNullPrecedence() {
        // H2 has a special null ordering and since group_concat does not support the nulls clause, we need the full emulation
        return false;
    }
}
