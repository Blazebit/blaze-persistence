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

package com.blazebit.persistence.impl.function.datediff.millisecond;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.TemplateRenderer;

/**
 * Inspired by http://stackoverflow.com/questions/22606161/difference-between-timestamps-in-milliseconds-in-oracle
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class OracleMillisecondDiffFunction extends MillisecondDiffFunction {

    private final TemplateRenderer bothParamRenderer;
    private final TemplateRenderer firstParamRenderer;
    private final TemplateRenderer secondParamRenderer;

    public OracleMillisecondDiffFunction() {
        super("1000 * (extract(second from cast(?2 as timestamp) - cast(?1 as timestamp))" +
                "    + 60 * (extract(minute from cast(?2 as timestamp) - cast(?1 as timestamp))" +
                "      + 60 * (extract(hour from cast(?2 as timestamp) - cast(?1 as timestamp))" +
                "        + 24 * (extract(day from cast(?2 as timestamp) - cast(?1 as timestamp))))))");
        this.bothParamRenderer = new TemplateRenderer("(select 1000 * (extract(second from t2 - t1)" +
                "    + 60 * (extract(minute from t2 - t1)" +
                "      + 60 * (extract(hour from t2 - t1)" +
                "        + 24 * (extract(day from t2 - t1))))) " +
                "from (select cast(?1 as timestamp) as t1, cast(?2 as timestamp) as t2 from dual))");
        this.firstParamRenderer = new TemplateRenderer("(select 1000 * (extract(second from cast(?2 as timestamp) - t1)" +
                "    + 60 * (extract(minute from cast(?2 as timestamp) - t1)" +
                "      + 60 * (extract(hour from cast(?2 as timestamp) - t1)" +
                "        + 24 * (extract(day from cast(?2 as timestamp) - t1))))) " +
                "from (select cast(?1 as timestamp) as t1 from dual))");
        this.secondParamRenderer = new TemplateRenderer("(select 1000 * (extract(second from t2 - cast(?1 as timestamp))" +
                "    + 60 * (extract(minute from t2 - cast(?1 as timestamp))" +
                "      + 60 * (extract(hour from t2 - cast(?1 as timestamp))" +
                "        + 24 * (extract(day from t2 - cast(?1 as timestamp)))))) " +
                "from (select cast(?2 as timestamp) as t2 from dual))");
    }

    @Override
    protected void renderDiff(FunctionRenderContext context) {
        boolean firstContains = context.getArgument(0).contains("?");
        boolean secondContains = context.getArgument(1).contains("?");
        if (firstContains && context.getArgument(0).length() != 1
                || secondContains && context.getArgument(1).length() != 1) {
            throw new IllegalArgumentException("Only simple parameters or expressions are allowed because of a needed reordering in SQL which is otherwise not possible! Expressions ['" + context.getArgument(0) + "', '" + context.getArgument(1) + "'] do not comply!");
        }
        if (firstContains) {
            if (secondContains) {
                bothParamRenderer.start(context).addArgument(0).addArgument(1).build();
            } else {
                firstParamRenderer.start(context).addArgument(0).addArgument(1).build();
            }
        } else if (secondContains) {
            secondParamRenderer.start(context).addArgument(0).addArgument(1).build();
        } else {
            // Reuse the expression multiple times
            renderer.start(context).addArgument(0).addArgument(1).build();
        }
    }
}
