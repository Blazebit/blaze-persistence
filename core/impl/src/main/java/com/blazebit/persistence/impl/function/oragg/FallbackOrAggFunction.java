/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.impl.function.oragg;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
public class FallbackOrAggFunction extends OrAggFunction {

    public static final FallbackOrAggFunction INSTANCE = new FallbackOrAggFunction();

    private FallbackOrAggFunction() {
    }

    @Override
    public void render(FunctionRenderContext context) {
        context.addChunk("MAX(");
        context.addArgument(0);
        context.addChunk(")");
    }

}
