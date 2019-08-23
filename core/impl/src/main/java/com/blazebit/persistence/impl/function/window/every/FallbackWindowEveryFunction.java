/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.impl.function.window.every;

import com.blazebit.persistence.impl.function.every.FallbackEveryFunction;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
public class FallbackWindowEveryFunction extends WindowEveryFunction {

    private final FallbackEveryFunction fallbackEveryFunction = new FallbackEveryFunction();

    public FallbackWindowEveryFunction(DbmsDialect dbmsDialect) {
        super(dbmsDialect);
    }

    @Override
    protected void render(FunctionRenderContext context, WindowFunction windowFunction) {
        super.render(context, windowFunction);
        fallbackEveryFunction.renderRhs(context);
    }

    @Override
    protected void renderFunction(FunctionRenderContext context, WindowFunction windowFunction) {
        fallbackEveryFunction.renderLhs(context);
    }

}
