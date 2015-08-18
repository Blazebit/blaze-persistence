/*
 * Copyright 2015 Blazebit.
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
package com.blazebit.persistence.impl.function.limit;

import com.blazebit.persistence.impl.function.CyclicUnsignedCounter;
import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * Uses a workaround for limit in IN predicates because of an limitation of MySQL.
 * See http://dev.mysql.com/doc/refman/5.0/en/subquery-restrictions.html for reference.
 *
 * @author Christian Beikov
 * @since 1.0.1
 */
public class MySQLLimitFunction extends LimitFunction {

    private static final ThreadLocal<CyclicUnsignedCounter> threadLocalCounter = new ThreadLocal<CyclicUnsignedCounter>() {

        @Override
        protected CyclicUnsignedCounter initialValue() {
            return new CyclicUnsignedCounter(-1);
        }

    };

    public MySQLLimitFunction() {
        super("(SELECT * FROM (?1 limit ?2) as ?3)", "(SELECT * FROM (?1 limit ?3, ?2) as ?4)");
    }

    @Override
    protected void renderLimitOffset(FunctionRenderContext functionRenderContext) {
        adapt(functionRenderContext, limitOffsetRenderer).addArgument(1)
            .addArgument(2)
            .addParameter("_tmp_" + threadLocalCounter.get().incrementAndGet())
            .build();
    }

    @Override
    protected void renderLimitOnly(FunctionRenderContext functionRenderContext) {
        adapt(functionRenderContext, limitOnlyRenderer).addArgument(1).addParameter("_tmp_" + threadLocalCounter.get().incrementAndGet()).build();
    }

}
