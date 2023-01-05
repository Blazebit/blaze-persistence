/*
 * Copyright 2014 - 2023 Blazebit.
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
