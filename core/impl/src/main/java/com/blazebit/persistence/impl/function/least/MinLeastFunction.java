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

package com.blazebit.persistence.impl.function.least;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MinLeastFunction extends AbstractLeastFunction {

    @Override
    public void render(FunctionRenderContext context) {
        int size = context.getArgumentsSize();
        if (size < 2) {
            throw new RuntimeException("The " + FUNCTION_NAME + " function needs at least two argument!");
        }
        context.addChunk("min(");
        context.addArgument(0);
        for (int i = 1; i < size; i++) {
            context.addChunk(", ");
            context.addArgument(i);
        }
        context.addChunk(")");
    }

}
