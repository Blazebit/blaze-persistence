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
package com.blazebit.persistence.impl.function.jsonset;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class PostgreSQLJsonSetFunction extends AbstractJsonSetFunction {

    @Override
    protected void render0(FunctionRenderContext context) {
        context.addChunk("jsonb_set(cast(");
        context.addArgument(0);
        context.addChunk(" as jsonb),");
        context.addChunk("'{");
        addUnquotedArgument(context, 2);
        for (int i = 3; i < context.getArgumentsSize(); i++) {
            context.addChunk(",");
            addUnquotedArgument(context, i);
        }
        context.addChunk("}',");
        context.addArgument(1);
        context.addChunk(")");
    }
}
