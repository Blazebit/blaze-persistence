/*
 * Copyright 2014 - 2020 Blazebit.
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
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.TemplateRenderer;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class YearWeekFunction implements JpqlFunction {

    protected final TemplateRenderer renderer;

    public YearWeekFunction() {
        // Work in PostgreSQL (verified), Oracle 10g (according to SO)
        // Does not work in H2, although it should. Interestingly, IYYY is returned correctly, but IW is not.
        // Does not work in MySQL, as it lacks the TO_CHAR method.
        this("TO_CHAR(?1, 'IYYY-IW')");
    }

    public YearWeekFunction(String template) {
        this.renderer = new TemplateRenderer(template);
    }

    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return true;
    }

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return String.class;
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() != 1) {
            throw new RuntimeException("The second function needs exactly one argument <datetime>! args=" + context);
        }

        renderDiff(context);
    }

    protected void renderDiff(FunctionRenderContext context) {
        renderer.start(context).addArgument(0).build();
    }
}
