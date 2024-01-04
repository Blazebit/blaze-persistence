/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.impl.function.datetime.yearweek;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class H2YearWeekFunction extends YearWeekFunction {

    public H2YearWeekFunction() {
        super("CONCAT(extract(year from DATE_TRUNC('week', ?1)), '-', iso_week(?1))");
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() == 0 || context.getArgument(0).contains("?")) {
            throw new RuntimeException("The second function does not support parameterized arguments for H2! args=" + context);
        }
        super.render(context);
    }
}
