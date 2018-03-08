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

package com.blazebit.persistence.impl.function.datediff.second;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.TemplateRenderer;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class MySQLSecondDiffFunction extends SecondDiffFunction {

    private final TemplateRenderer bothParamRenderer;

    public MySQLSecondDiffFunction() {
        super("truncate(unix_timestamp(?2), 0) - truncate(unix_timestamp(?1), 0)");
        this.bothParamRenderer = new TemplateRenderer("(select truncate(unix_timestamp(t2), 0) - truncate(unix_timestamp(t1), 0) from (select ?1 as t1, ?2 as t2) as temp)");
    }

    @Override
    protected void renderDiff(FunctionRenderContext context) {
        boolean firstContains = context.getArgument(0).contains("?");
        boolean secondContains = context.getArgument(1).contains("?");
        if (firstContains && context.getArgument(0).length() != 1
                || secondContains && context.getArgument(1).length() != 1) {
            throw new IllegalArgumentException("Only simple parameters or expressions are allowed because of a needed reordering in SQL which is otherwise not possible! Expressions ['" + context.getArgument(0) + "', '" + context.getArgument(1) + "'] do not comply!");
        }
        if (firstContains & secondContains) {
            bothParamRenderer.start(context).addArgument(0).addArgument(1).build();
        } else {
            // Reuse the expression multiple times
            renderer.start(context).addArgument(0).addArgument(1).build();
        }
    }
}
