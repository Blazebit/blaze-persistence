/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.impl.function.pageposition;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class TransactSQLPagePositionFunction extends PagePositionFunction {

    private static final String ROWNUM_FUNCTION = "row_number() over(order by (select 1))";

    public TransactSQLPagePositionFunction() {
        // Although it is not perfectly safe to rely on this, it works and regarding positional parameters it is also the least hassle
        // If we wanted to support this in a "non-dirty" way, we'd have to parse the sql, understand it, and the completely regenerate it
        // which is not only an overkill, but apparently also unnecessary
        super("(select base1_.rownumber_ from (select " + ROWNUM_FUNCTION
                + " as rownumber_, base_.* from (select top 2147483647 ?1 as base_) as base1_ where ?2 = base1_.?3)");
    }

    @Override
    protected String getRownumFunction() {
        return ROWNUM_FUNCTION;
    }

    @Override
    protected void renderPagePosition(FunctionRenderContext functionRenderContext, String idName) {
        String subquery = functionRenderContext.getArgument(0);
        String subqueryStart = "(select ";

        renderer.start(functionRenderContext).addParameter(subquery.substring(subqueryStart.length())).addArgument(1).addParameter(idName).build();
    }
}
